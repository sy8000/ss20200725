package cn.besbing.Cron;

import cn.besbing.CommonUtils.MaintainModel.SpringUtil;
import cn.besbing.CommonUtils.MaintainModel.UpdateLimsBillStatus;
import cn.besbing.Entities.*;
import cn.besbing.Service.Impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

//@Component
public class AnalysisThread {

    private  static final String CRON = "0 0 0 * * ?";

    private Logger logger = LoggerFactory.getLogger(AnalysisThread.class);

    private CustomerSqlServiceImpl customerSqlService = SpringUtil.getBean(CustomerSqlServiceImpl.class);

    private INcAnalysisListServiceImpl iNcAnalysisListService = SpringUtil.getBean(INcAnalysisListServiceImpl.class);

    private IAnalysisServiceImpl iAnalysisService = SpringUtil.getBean(IAnalysisServiceImpl.class);



    /**
     * 获取analysis表中存在、但nc_analysis_list表中不存在的条目,判断依据:analysis.name、analysis.version
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    //@Scheduled(cron = "0 0 1 * * ?")
    public void onlyAddAnalysisThread() throws Exception {
        Analysis analysis = new Analysis();
        NcAnalysisList ncAnalysisList = null;
        List<Analysis> analysisReturnList = new ArrayList<>();
        List<Analysis> analysisAllList = new ArrayList<>();
        try{
            logger.info("开始获取全部analysis分析..........");
            analysisAllList  = iAnalysisService.selectAnalysisByNothing();
            logger.info("结束获取全部analysis分析..........");
        }catch (Exception e){
            logger.error("analysis查询错误");
        }
        if (analysisAllList.size() > 0){
            for (Analysis allanalysislist : analysisAllList){
                /*if ("非标-耐久性".equals(allanalysislist.getName())){
                    logger.info("...................................................................");
                }*/
                ncAnalysisList = new NcAnalysisList();
                ncAnalysisList.setName(allanalysislist.getName());
                ncAnalysisList.setVersion(allanalysislist.getVersion());
                try{
                    ncAnalysisList = iNcAnalysisListService.selectByAnalysisKey(ncAnalysisList);
                    //如果查到，什么也不做，如果查不到立即更新
                    if (ncAnalysisList == null){
                        logger.info("================================={}===============================",allanalysislist.getName());
                        logger.info("开始查询添加analysis语句...........");
                        List<String> addAnalysisSqls = customerSqlService.selectAsList("select sql_text from sql_records where sql_code like 'analysis%' ");
                        logger.info("结束查询添加analysis语句...........");
                        for (String analysisSql : addAnalysisSqls){
                            //逐条插入分析，每个分析三条语句
                            analysisSql = analysisSql.replace("sheny",allanalysislist.getName());
                            analysisSql = analysisSql.replace("why",String.valueOf(allanalysislist.getVersion()));
                            try{
                                logger.info("开始执行语句{}..........",analysisSql);
                                customerSqlService.insert(analysisSql);
                                logger.info("结束执行语句{}..........",analysisSql);
                            }catch (Exception e) {
                                logger.error("分析添加错误...语句：{}", analysisSql);
                            }
                        }
                    }
                }catch (Exception e){
                    logger.info("在nc_analysis_list:analysis表中查询出错........cause:{}" , e.getStackTrace());
                }
            }
        }else {
            throw new Exception("查询analysis数量小于1");
        }
        //return analysisReturnList;
    }

    /**
     * 逻辑：
     * 首先获取product表，然后去与NC_PROD_LIST表比较，没有则插入
     * 获取test_list表，去与NC_TEST_LIST比较，没有则插入
     * 通过product表的name、code去判断NC_BASPROD_NAME/NC_BASPROD_TYPE/NC_BASEN_TYPE
     * 获取product_grade表，确定NC_BASPROD_POINT/NC_BASPROD_STRUCT表
     * 通过product,判断NC_BASPROD_CONTACT表
     * 通过prod_grade_STAGE判断NC_BASPROD_TEMP
     * 插入NC_SAMPLE_INFO
     * 通过product表插入NC_TEST_INIT
     * 通过test_list名，判断NC_TASK_ADDUNION
     * 通过test_list_comp。test_list判断NC_TESTLIST_COMP
     * 通过product判断nc_test_after
     *
     * @throws Exception
     */

    private IProductServiceImpl iProductService = SpringUtil.getBean(IProductServiceImpl.class);
    private INcProdListServiceImpl iNcProdListService = SpringUtil.getBean(INcProdListServiceImpl.class);


    @Transactional(rollbackFor = Exception.class)
    //@Scheduled(cron = "0 0 4 * * ?")
    public void addTestListThread() throws Exception {
        //获取所有在用product
        List<Product> productsList = new ArrayList<>();
        try{
            productsList = iProductService.getAllProduct();
        }catch (Exception e){
            logger.error("获取全部product时出错...");
        }
        if (productsList.size() < 1){
            logger.info("获取的product总数小于1");
            throw new Exception("获取的product总数小于1");
        }else{
            //product的list不为0时才正式运行
            String productName = "";
            String productCode = "";
            String productTestlist = "";
            String productVersion = "";
            NcProdListKey ncProdListKey = null;
            String executeSql = "";
            logger.info("product数量>0，testlist同步正式开始......");
            /**
             * 开始定义所有需组装的vo
             */
            NcProdList ncProdList = new NcProdList();


            for(Product product : productsList){
                ncProdListKey = new NcProdListKey();
                ncProdListKey.setName(product.getName());
                ncProdListKey.setVersion(product.getVersion());
                /*productName = product.getName();
                productCode = product.getCode();
                productTestlist = product.getTestList();
                productVersion = String.valueOf(product.getVersion());*/
                /************
                 *
                 * 2020/6/6  sheny review
                 * content : 每张表返回pk存入相应变量，下面用时直接拿
                 *
                 ***********/
                try{
                    ncProdList = iNcProdListService.getNcProdListByPrimary(ncProdListKey);
                }catch (Exception e){
                    logger.error("ncProdList查询出错...cause:{}",e.getStackTrace());
                }
                if (ncProdList == null){
                    //如果不存在，则开始更新
                    logger.info("product:{},version:{},开始更新...",product.getName(),product.getVersion());
                    try {
                        logger.info("开始组装nc_prod_list...");
                        executeSql = getExecuteSql("testlist_nc_prod_list",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_prod_list...");
                    }catch (Exception e){
                        logger.error("更新nc_prod_list表时出现异常：{}",e.getStackTrace());
                    }
                    try{
                        logger.info("开始组装nc_test_list...");
                        executeSql = getExecuteSql("testlist_nc_test_list",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_test_list...");
                    }catch(Exception e){
                        logger.error("更新nc_test_list表时出现异常：{}",e.getStackTrace());
                    }
                    try{
                        logger.info("开始组装nc_basprod_name...");
                        executeSql = getExecuteSql("testlist_nc_basprod_name",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_basprod_name...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_name出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_basprod_type...");
                        executeSql = getExecuteSql("testlist_nc_basprod_type",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_basprod_type...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_type出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装NC_BASEN_TYPE...");
                        executeSql = getExecuteSql("testlist_nc_basen_type",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装NC_BASEN_TYPE...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_type出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_basprod_point...");
                        executeSql = getExecuteSql("testlist_nc_basprod_point",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_basprod_point...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_point出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_basprod_struct...");
                        executeSql = getExecuteSql("testlist_nc_basprod_struct",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_basprod_struct...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_struct出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装NC_BASPROD_CONTACT...");
                        executeSql = getExecuteSql("test_nc_basprod_contact",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装NC_BASPROD_CONTACT...");
                    }catch(Exception e){
                        logger.error("组装NC_BASPROD_CONTACT出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_basprod_temp...");
                        executeSql = getExecuteSql("testlist_nc_basprod_temp",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_basprod_temp...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_temp出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_sample_info...");
                        executeSql = getExecuteSql("testlist_nc_sample_info",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_sample_info...");
                    }catch(Exception e){
                        logger.error("组装nc_sample_info出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_test_init...");
                        executeSql = getExecuteSql("testlist_nc_test_init",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_test_init...");
                    }catch(Exception e){
                        logger.error("组装nc_test_init出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_task_addunion...");
                        executeSql = getExecuteSql("testlist_nc_task_addunion",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_task_addunion...");
                    }catch(Exception e){
                        logger.error("组装nc_task_addunion出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_testlist_comp...");
                        executeSql = getExecuteSql("testlist_nc_testlist_comp",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_testlist_comp...");
                    }catch(Exception e){
                        logger.error("组装nc_testlist_comp出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_test_after...");
                        executeSql = getExecuteSql("testlist_nc_test_after",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_test_after...");
                    }catch(Exception e){
                        logger.error("组装nc_test_after出错：{}",e.getStackTrace());
                    }
                    logger.info("product:{},version:{},更新完成...",product.getName(),product.getVersion());
                }
            }
            logger.info("testlist同步正常结束......");
        }

    }



    public String getExecuteSql(String modules,Product product){
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
