package cn.besbing.CommonUtils.MaintainModel;

import cn.besbing.Entities.Result;
import cn.besbing.Entities.ResultDefKey;
import cn.besbing.Service.Impl.CustomerSqlServiceImpl;
import cn.besbing.Service.Impl.ResultServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class LimsResultOpration {


    private Logger logger = LoggerFactory.getLogger(UpdateLimsBillStatus.class);


    private ResultServiceImpl resultService = SpringUtil.getBean(ResultServiceImpl.class);

    private CustomerSqlServiceImpl customerSqlService = SpringUtil.getBean(CustomerSqlServiceImpl.class);

    /**
     * 拿到小垂的result结果，封装ResultThreePrimary实体，然后查询
     * 有：则update
     * 无：则insert
     * 返回ResultThreePrimary.toString()
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String paramProcessor(Result res){
        ResultDefKey resultDefKey = new ResultDefKey();
        resultDefKey.setName(res.getName());
        resultDefKey.setSampleNumber(res.getSampleNumber());
        resultDefKey.setTestNumber(res.getTestNumber());
        Result result = resultService.getResultByDefKey(res);
        //判断是否存在此条记录
        if (result != null){
            //有记录，更新
            result.setTestNumber(res.getTestNumber());
            result.setSampleNumber(res.getSampleNumber());
            result.setName(res.getName());
            result.setMinimum(res.getMinimum());
            result.setMaximum(res.getMaximum());
            result.setEntry(res.getEntry());
            result.setEnteredBy(res.getEnteredBy());
            result.setFormattedEntry(res.getFormattedEntry());
            try {
                logger.info("开始更新{}的结果数据,class:{}",resultDefKey.toString(),this.getClass().getName());
                resultService.updateByPrimary(result);
                logger.info("更新完成{}的结果数据,class:{}",resultDefKey.toString(),this.getClass().getName());
            }catch (Exception e){
                logger.error("更新错误{}的结果数据,class:{}",resultDefKey.toString(),this.getClass().getName());
            }
        }else {
            //没记录，新增
            Integer incMax_Result = Integer.valueOf(customerSqlService.selectOne("select * from increments s where s.type = 'RESULT'"));
            Integer actualMax_Result = Integer.valueOf(customerSqlService.selectOne("select max(result_number) from result"));
            Integer target_resultNumber = 0;
            if (actualMax_Result > incMax_Result){
                try{
                    logger.info("开始更新increments的resultnumber,result_number:{},class:{}",actualMax_Result+1,this.getClass().getName());
                    customerSqlService.update("update increments s set s.value = "+ actualMax_Result + 1 +" where type = 'RESULT'");
                    logger.info("完成更新increments的resultnumber,result_number:{},class:{}",actualMax_Result+1,this.getClass().getName());
                }catch (Exception e){
                    logger.error("更新increments的resultnumber出错,result_number:{},class:{},message:{}",actualMax_Result+1,this.getClass().getName(),e.getStackTrace());
                }
            }
            //开始准备insert
            result.setTestNumber(res.getTestNumber());
            result.setSampleNumber(res.getSampleNumber());
            result.setName(res.getName());
            result.setMinimum(res.getMinimum());
            result.setMaximum(res.getMaximum());
            result.setEntry(res.getEntry());
            result.setEnteredBy(res.getEnteredBy());
            result.setFormattedEntry(res.getFormattedEntry());
            result.setResultNumber(Long.valueOf(actualMax_Result + 1));
            try{
                logger.info("开始插入result_number为{}的记录,class:{}",actualMax_Result+1,this.getClass().getName());
                resultService.insertResult(result);
                logger.info("结束插入result_number为{}的记录,class:{}",actualMax_Result+1,this.getClass().getName());
            }catch (Exception e){
                logger.error("更新increments的resultnumber出错,result_number:{},class:{},message:{}",actualMax_Result+1,this.getClass().getName(),e.getStackTrace());
            }
        }
        if (result.getResultNumber() != null){
            return result.getResultNumber().toString();
        }else {
            return null;
        }

    }

}
