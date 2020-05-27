package cn.besbing.CommonUtils.MaintainModel;

import cn.besbing.Dao.CustomerSqlMapper;
import cn.besbing.Service.ICustomerSqlService;
import cn.besbing.Service.Impl.CustomerSqlServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateLimsBillStatus {

    private Logger logger = LoggerFactory.getLogger(UpdateLimsBillStatus.class);





    /**
     * 流程跳转至样品管理员
     * @param projectList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int moveToSampleMgr(List<String> projectList){
        int flag = 0;
        CustomerSqlServiceImpl customerSqlService = SpringUtil.getBean(CustomerSqlServiceImpl.class);
        List<String> sqlList = new ArrayList();
        sqlList = customerSqlService.selectAsList("select sql_text from sql_records where sql_code like 'sample%' ");
        for (String projName : projectList){
            logger.info("开始处理名称为{}的委托单，跳转至样品管理员处......",projName);
            for (String sql : sqlList){
                try{
                    flag += customerSqlService.update(sql.replace("sheny",projName));
                }catch (Exception e){
                    logger.error("处理名称为{}的委托单跳转至样品管理员时出错......",projName);
                }
            }
            logger.info("结束处理名称为{}的委托单，跳转至样品管理员处......",projName);
        }
        return flag;
    }

}
