package cn.besbing;

import cn.besbing.CommonUtils.MaintainModel.SpringUtil;
import cn.besbing.Cron.AnalysisThread;
import cn.besbing.Entities.NcAnalysisList;
import cn.besbing.Entities.NcProdList;
import cn.besbing.Entities.Product;
import cn.besbing.Service.Impl.CustomerSqlServiceImpl;
import cn.besbing.Service.Impl.INcAnalysisListServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DloadercloudApplication.class)
public class TestDatabase {
    private Logger logger = LoggerFactory.getLogger(AnalysisThread.class);
    //private CustomerSqlServiceImpl customerSqlService = SpringUtil.getBean(CustomerSqlServiceImpl.class);
    @Autowired
    CustomerSqlServiceImpl customerSqlService = new CustomerSqlServiceImpl();
    @Autowired
    INcAnalysisListServiceImpl iNcAnalysisListService;
    //UL60947-4-1-2014_8.2.4-1A

    @Test
    public void testVo(){
        AnalysisThread analysisThread = new AnalysisThread();
        try{
            //执行分析添加
            //analysisThread.onlyAddAnalysisThread();
            //执行testlist
            //analysisThread.addTestListThread();
            logger.info("执行完毕");
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        /*NcAnalysisList ncAnalysisList = new NcAnalysisList();
        //NcAnalysisList ncAnalysisList1 = new NcAnalysisList();
        ncAnalysisList.setName("UL60947-4-1-2014_8.2.4-1A");
        ncAnalysisList.setVersion(Long.valueOf(1));
        try{
            ncAnalysisList = iNcAnalysisListService.selectByAnalysisKey(ncAnalysisList);
            logger.info("======================{}+++++++++=========",ncAnalysisList.getNcAnalysisCode());
        }catch(Exception e){
            e.printStackTrace();
        }*/
        /*String executeSql = "";
        Product product = new Product();
        NcProdList ncProdList = null;
        product.setName("QFVF4284-2014");
        product.setVersion(Long.valueOf(1));
        product.setCode("Q/FVF 4284-2014");
        product.setTestList("QFVF4284-2017_HF8654");
        executeSql = getExecuteSql("testlist_nc_prod_list",product);
        List<?> list = new ArrayList<>();
        //ncProdList = (NcProdList)customerSqlService.selectList(executeSql);
        list = customerSqlService.selectList(executeSql);
        JSONArray array = JSONArray.parseArray(JSON.toJSONString(list));*/


    }


    public String getExecuteSql(String modules, Product product){
        String sql = "";
        try{
            logger.info("开始获取模块语句,module:{}..............",modules);
            sql = customerSqlService.selectOne("select sql_text from sql_records where sql_code = '" + modules + "'");
            //语句替换
            /**
             * sheny : product.name
             * why   : product.version
             * shenycode : product.code
             * shenytestlist : product.testlist
             */
            sql = sql.replace("sheny",product.getName());
            sql = sql.replace("why",String.valueOf(product.getVersion()));
            sql = sql.replace("shenycode",product.getCode());
            sql = sql.replace("shenytestlist",product.getTestList());
            logger.info("结束获取模块语句,module:{}..............",modules);
        }catch (Exception e){
            logger.error("获取语句出错,modules:{},cause:{}",modules,e.getStackTrace());
        }
        return sql;
    }


}
