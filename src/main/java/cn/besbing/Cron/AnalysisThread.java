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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private INcTestListServiceImpl iNcTestListService = SpringUtil.getBean(INcTestListServiceImpl.class);
    private NcBaseprodNameServiceImpl ncBaseprodNameService = SpringUtil.getBean(NcBaseprodNameServiceImpl.class);
    private INcBasprodTypeServiceImpl iNcBasprodTypeService = SpringUtil.getBean(INcBasprodTypeServiceImpl.class);
    private INcBasenTypeServiceImpl iNcBasenTypeService = SpringUtil.getBean(INcBasenTypeServiceImpl.class);
    private INcBasprodPointServiceImpl iNcBasprodPointService = SpringUtil.getBean(INcBasprodPointServiceImpl.class);
    private INcBasprodStruceServiceImpl iNcBasprodStruceService = SpringUtil.getBean(INcBasprodStruceServiceImpl.class);
    private INcBasprodContactServiceImpl iNcBasprodContactService = SpringUtil.getBean(INcBasprodContactServiceImpl.class);
    private INcSampleInfoServiceImpl iNcSampleInfoService = SpringUtil.getBean(INcSampleInfoServiceImpl.class);

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
                    /*
                    开始定义各vo
                     */
                    NcProdList insertncProdList = new NcProdList();
                    NcTestList inserttestList = new NcTestList();
                    NcBasprodName insertncBasprodName = new NcBasprodName();
                    NcBasprodType insertncBasprodType = new NcBasprodType();
                    NcBasenType insertncBasenType = new NcBasenType();
                    NcBasprodPoint insertncBasprodPoint = new NcBasprodPoint();
                    List<String> pkBasprodPoint = new ArrayList<>();
                    NcBasprodStruct insertncBasprodStruct = new NcBasprodStruct();
                    List<String> pkBasprodStruct = new ArrayList<>();
                    NcBasprodContact insertncBasprodContact = new NcBasprodContact();
                    NcBasprodTemp insertncBasprodTemp = new NcBasprodTemp();
                    List<String> pkBasprodTemp = new ArrayList<>();
                    NcSampleInfo insertncSampleInfo = new NcSampleInfo();
                    List<String> pkSampleInfo = new ArrayList<>();
                    NcTestInit insertncTestInit = new NcTestInit();
                    NcTaskAddunion insertncTaskAddunion = new NcTaskAddunion();
                    NcTestlistComp insertncTestListComp = new NcTestlistComp();
                    NcTestAfter insertncTestAfter = new NcTestAfter();

                    try {
                        logger.info("开始组装nc_prod_list...");
                        executeSql = getExecuteSql("testlist_nc_prod_list",product);
                        //insertncProdList = customerSqlService.selectAsList(executeSql);
                        List<Map<String,Object>> list =  customerSqlService.selectList(executeSql);
                        for (Map<String,Object> map : list){
                            //logger.info(map.get("PK_PROD_LIST").toString());
                            insertncProdList.setPkProdList(map.get("PK_PROD_LIST").toString());
                            insertncProdList.setName(map.get("NAME").toString());
                            insertncProdList.setVersion(Long.valueOf(map.get("VERSION").toString()));
                            insertncProdList.setNcProdCode(map.get("NC_PROD_CODE").toString());
                            insertncProdList.setNcProdName(map.get("NC_PROD_NAME").toString());
                            insertncProdList.setNcProdIsdisable(BigDecimal.valueOf(Long.valueOf(map.get("NC_PROD_ISDISABLE").toString())));
                            insertncProdList.setDef1(map.get("DEF1").toString());
                            insertncProdList.setDef2(map.get("DEF2").toString());
                            insertncProdList.setDef3(map.get("DEF3").toString());
                            insertncProdList.setDef4(map.get("DEF4").toString());
                            insertncProdList.setDef5(map.get("DEF5").toString());
                            insertncProdList.setNcProdDes(map.get("NC_PROD_DES").toString());
                            insertncProdList.setNcProdAlias(map.get("NC_PROD_ALIAS").toString());
                            iNcProdListService.insert(insertncProdList);
                        }
                        logger.info("结束组装nc_prod_list...");
                    }catch (Exception e){
                        logger.error("更新nc_prod_list表时出现异常：{}",e.getStackTrace());
                    }
                    try{
                        logger.info("开始组装nc_test_list...");
                        executeSql = getExecuteSql("testlist_nc_test_list",product);
                        customerSqlService.insert(executeSql);
                        List<Map<String,Object>> list =  customerSqlService.selectList(executeSql);
                        for (Map<String,Object> map : list){
                            inserttestList.setPkTestList(map.get("PK_TEST_LIST").toString());
                            inserttestList.setName(map.get("NAME").toString());
                            inserttestList.setNcTestlistCode(map.get("NC_TESTLIST_CODE").toString());
                            inserttestList.setNcTestlistName(map.get("NC_TESTLIST_NAME").toString());
                            inserttestList.setNcTestlistIsdisable(BigDecimal.valueOf(Long.valueOf(map.get("NC_TESTLIST_ISDISABLE").toString())));
                            inserttestList.setDef1(map.get("DEF1").toString());
                            inserttestList.setDef2(map.get("DEF2").toString());
                            inserttestList.setDef3(map.get("DEF3").toString());
                            inserttestList.setDef4(map.get("DEF4").toString());
                            inserttestList.setDef5(map.get("DEF5").toString());
                            iNcTestListService.insert(inserttestList);
                        }
                        logger.info("结束组装nc_test_list...");
                    }catch(Exception e){
                        logger.error("更新nc_test_list表时出现异常：{}",e.getStackTrace());
                    }
                    try{
                        logger.info("开始组装nc_basprod_name...");
                        executeSql = getExecuteSql("testlist_nc_basprod_name",product);
                        List<Map<String,Object>> list =  customerSqlService.selectList(executeSql);
                        for (Map<String,Object> map : list){
                            insertncBasprodName.setPkBasprodName(map.get("PK_BASPROD_NAME").toString());
                            insertncBasprodName.setPkBasprodName(map.get("NC_BASPROD_CODE").toString());
                            insertncBasprodName.setPkBasprodName(map.get("NC_BASPROD_NAME").toString());
                            insertncBasprodName.setPkBasprodName(map.get("VDEF1").toString().trim());
                            insertncBasprodName.setPkBasprodName(map.get("VDEF2").toString());
                            insertncBasprodName.setPkBasprodName(map.get("VDEF3").toString());
                            insertncBasprodName.setPkBasprodName(map.get("VDEF4").toString());
                            insertncBasprodName.setPkBasprodName(map.get("VDEF5").toString());
                            ncBaseprodNameService.insert(insertncBasprodName);
                        }
                        logger.info("结束组装nc_basprod_name...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_name出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_basprod_type...");
                        executeSql = getExecuteSql("testlist_nc_basprod_type",product);
                        List<Map<String,Object>> list =  customerSqlService.selectList(executeSql);
                        for (Map<String,Object> map : list){
                            insertncBasprodType.setPkBasprodType(map.get("PK_BASPROD_TYPE").toString());
                            insertncBasprodType.setNcBasprodtypeCode(map.get("NC_BASPRODTYPE_CODE").toString());
                            insertncBasprodType.setNcBasprodtypeName(map.get("NC_BASPRODTYPE_NAME").toString());
                            insertncBasprodType.setVdef1(map.get("VDEF1").toString().trim());
                            insertncBasprodType.setVdef2(map.get("VDEF2").toString().trim());
                            insertncBasprodType.setVdef3(map.get("VDEF3").toString());
                            insertncBasprodType.setVdef4(map.get("VDEF4").toString());
                            insertncBasprodType.setVdef5(map.get("VDEF5").toString());
                            iNcBasprodTypeService.insert(insertncBasprodType);
                        }
                        logger.info("结束组装nc_basprod_type...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_type出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装NC_BASEN_TYPE...");
                        executeSql = getExecuteSql("testlist_nc_basen_type",product);
                        List<Map<String,Object>> list =  customerSqlService.selectList(executeSql);
                        for (Map<String,Object> map : list){
                            insertncBasenType.setPkBasenType(map.get("PK_BASEN_TYPE").toString());
                            insertncBasenType.setPkBasprodType(insertncBasprodType.getPkBasprodType().toString());
                            insertncBasenType.setNcBasenCode(map.get("NC_BASEN_CODE").toString());
                            insertncBasenType.setNcBbasenName(map.get("NC_BBASEN_NAME").toString());
                            insertncBasenType.setVdef1(map.get("VDEF1").toString());
                            insertncBasenType.setVdef2(map.get("VDEF2").toString());
                            insertncBasenType.setVdef3(map.get("VDEF3").toString());
                            insertncBasenType.setVdef4(map.get("VDEF4").toString());
                            insertncBasenType.setVdef5(map.get("VDEF5").toString());
                            iNcBasenTypeService.insert(insertncBasenType);
                        }
                        logger.info("结束组装NC_BASEN_TYPE...");
                    }catch(Exception e){
                        logger.error("组装NC_BASEN_TYPE出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_basprod_point...");
                        executeSql = getExecuteSql("testlist_nc_basprod_point",product);
                        List<Map<String,Object>> list =  customerSqlService.selectList(executeSql);
                        pkBasprodPoint.clear();
                        for (Map<String,Object> map : list){
                            pkBasprodPoint.add(map.get("PK_BASPROD_POINT").toString());
                            insertncBasprodPoint.setPkBasprodPoint(map.get("PK_BASPROD_POINT").toString());
                            insertncBasprodPoint.setNcBasprodpointCode(map.get("NC_BASPRODPOINT_CODE").toString());
                            insertncBasprodPoint.setNcBasprodpointName(map.get("NC_BASPRODPOINT_NAME").toString());
                            insertncBasprodPoint.setVdef1(map.get("VDEF1").toString());
                            insertncBasprodPoint.setVdef2(map.get("VDEF2").toString());
                            insertncBasprodPoint.setVdef3(map.get("VDEF3").toString());
                            insertncBasprodPoint.setVdef4(map.get("VDEF4").toString());
                            insertncBasprodPoint.setVdef5(map.get("VDEF5").toString());
                            iNcBasprodPointService.insert(insertncBasprodPoint);
                        }
                        logger.info("结束组装nc_basprod_point...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_point出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_basprod_struct...");
                        executeSql = getExecuteSql("testlist_nc_basprod_struct",product);
                        List<Map<String,Object>> list =  customerSqlService.selectList(executeSql);
                        pkBasprodStruct.clear();
                        for (Map<String,Object> map : list){
                            pkBasprodStruct.add(map.get("PK_BASPROD_STRUCT").toString());
                            insertncBasprodStruct.setPkBasprodStruct(map.get("PK_BASPROD_STRUCT").toString());
                            insertncBasprodStruct.setPkBasprodStruct(map.get("NC_BASPRODSTRUCT_CODE").toString());
                            insertncBasprodStruct.setPkBasprodStruct(map.get("NC_BASPRODSTRUCT_NAME").toString());
                            insertncBasprodStruct.setPkBasprodStruct(map.get("VDEF1").toString());
                            insertncBasprodStruct.setPkBasprodStruct(map.get("VDEF2").toString());
                            insertncBasprodStruct.setPkBasprodStruct(map.get("VDEF3").toString());
                            insertncBasprodStruct.setPkBasprodStruct(map.get("VDEF4").toString());
                            insertncBasprodStruct.setPkBasprodStruct(map.get("VDEF5").toString());
                            iNcBasprodStruceService.insert(insertncBasprodStruct);
                        }
                        logger.info("结束组装nc_basprod_struct...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_struct出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装NC_BASPROD_CONTACT...");
                        executeSql = getExecuteSql("test_nc_basprod_contact",product);
                        List<Map<String,Object>> list =  customerSqlService.selectList(executeSql);
                        for (Map<String,Object> map : list){
                            insertncBasprodContact.setPkBasprodContact(map.get("PK_BASPROD_CONTACT").toString());
                            insertncBasprodContact.setNcBasprodcontactCode(map.get("NC_BASPRODCONTACT_CODE").toString());
                            insertncBasprodContact.setNcBasprodcontactName(map.get("NC_BASPRODCONTACT_NAME").toString());
                            insertncBasprodContact.setVdef1(map.get("VDEF1").toString());
                            insertncBasprodContact.setVdef2(map.get("VDEF2").toString());
                            insertncBasprodContact.setVdef3(map.get("VDEF3").toString());
                            insertncBasprodContact.setVdef4(map.get("VDEF4").toString());
                            insertncBasprodContact.setVdef5(map.get("VDEF5").toString());
                            iNcBasprodContactService.insert(insertncBasprodContact);
                        }
                        logger.info("结束组装NC_BASPROD_CONTACT...");
                    }catch(Exception e){
                        logger.error("组装NC_BASPROD_CONTACT出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_basprod_temp...");
                        executeSql = getExecuteSql("testlist_nc_basprod_temp",product);
                        List<Map<String,Object>> list =  customerSqlService.selectList(executeSql);
                        pkBasprodTemp.clear();
                        for (Map<String,Object> map : list){
                            insertncBasprodTemp.setPkBasprodTemp(map.get("PK_BASPROD_TEMP").toString());
                            pkBasprodTemp.add(map.get("PK_BASPROD_TEMP").toString());
                            insertncBasprodTemp.setNcBasprodtempCode(map.get("NC_BASPRODTEMP_CODE").toString());
                            insertncBasprodTemp.setNcBasprodtempName(map.get("NC_BASPRODTEMP_NAME").toString());
                            insertncBasprodTemp.setVdef1(map.get("VDEF1").toString());
                            insertncBasprodTemp.setVdef2(map.get("VDEF2").toString());
                            insertncBasprodTemp.setVdef3(map.get("VDEF3").toString());
                            insertncBasprodTemp.setVdef4(map.get("VDEF4").toString());
                            insertncBasprodTemp.setVdef5(map.get("VDEF5").toString());
                        }
                        logger.info("结束组装nc_basprod_temp...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_temp出错：{}",e.getStackTrace());
                    }

                    try{
                        logger.info("开始组装nc_sample_info...");
                        //executeSql = getExecuteSql("testlist_nc_sample_info",product);
                        String sql1 = customerSqlService.selectOne("select sql_text from sql_records where sql_code = 'testlist_nc_sample_info1'");
                        String sql2 = customerSqlService.selectOne("select sql_text from sql_records where sql_code = 'testlist_nc_sample_info2'");
                        executeSql = sql1 + sql2;
                        executeSql = executeSql.replace("sheny",product.getName());
                        executeSql = executeSql.replace("why",String.valueOf(product.getVersion()));
                        executeSql = executeSql.replace("shenycode",product.getCode());
                        executeSql = executeSql.replace("shenytestlist",product.getTestList());
                        String finalSql = "";
                        //数据库中语句获取完成，开始处理语句
                        for(String point : pkBasprodPoint){
                            for (String struct : pkBasprodStruct){
                                for(String temp : pkBasprodTemp){
                                    finalSql = executeSql;
                                    finalSql = finalSql.replace("",insertncProdList.getPkProdList());
                                    finalSql = finalSql.replace("",inserttestList.getPkTestList());
                                    finalSql = finalSql.replace("",insertncBasprodName.getPkBasprodName());
                                    finalSql = finalSql.replace("",insertncBasprodType.getPkBasprodType());
                                    finalSql = finalSql.replace("",insertncBasenType.getPkBasenType());
                                    finalSql = finalSql.replace("",point);
                                    finalSql = finalSql.replace("",struct);
                                    finalSql = finalSql.replace("",insertncBasprodContact.getPkBasprodContact());
                                    finalSql = finalSql.replace("",temp);
                                    List<Map<String,Object>> list =  customerSqlService.selectList(finalSql);
                                    if (list != null){
                                        for (Map<String,Object> map : list){
                                            pkSampleInfo.add(map.get("").toString());
                                            insertncSampleInfo.setPkSampleInfo(map.get("").toString());
                                            /***
                                             * e.g.   添加各对应值
                                             */
                                            iNcSampleInfoService.insert(insertncSampleInfo);
                                        }
                                    }
                                }
                            }
                        }
                        logger.info("结束组装nc_sample_info...");
                    }catch(Exception e){
                        logger.error("组装nc_sample_info出错：{}",e.getStackTrace());
                    }

                    /*try {
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
                    -----------------------
                    try{
                        logger.info("开始组装nc_basprod_point...");
                        executeSql = getExecuteSql("testlist_nc_basprod_point",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_basprod_point...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_point出错：{}",e.getStackTrace());
                    }
                    ---------------------------
                    try{
                        logger.info("开始组装nc_basprod_struct...");
                        executeSql = getExecuteSql("testlist_nc_basprod_struct",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_basprod_struct...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_struct出错：{}",e.getStackTrace());
                    }
                    ------------------------------------------------
                    try{
                        logger.info("开始组装NC_BASPROD_CONTACT...");
                        executeSql = getExecuteSql("test_nc_basprod_contact",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装NC_BASPROD_CONTACT...");
                    }catch(Exception e){
                        logger.error("组装NC_BASPROD_CONTACT出错：{}",e.getStackTrace());
                    }
                    --------------------------------------------------------
                    try{
                        logger.info("开始组装nc_basprod_temp...");
                        executeSql = getExecuteSql("testlist_nc_basprod_temp",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_basprod_temp...");
                    }catch(Exception e){
                        logger.error("组装nc_basprod_temp出错：{}",e.getStackTrace());
                    }
                    --------------------------------------------------
                    try{
                        logger.info("开始组装nc_sample_info...");
                        executeSql = getExecuteSql("testlist_nc_sample_info",product);
                        customerSqlService.insert(executeSql);
                        logger.info("结束组装nc_sample_info...");
                    }catch(Exception e){
                        logger.error("组装nc_sample_info出错：{}",e.getStackTrace());
                    }
                    ----------------------------------------------------------
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
                     */
                    /**
                     *      sheny 2018.09.23
                     *      version : init
                     *      Func : Sync from another Database(Asyn)
                     */
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
