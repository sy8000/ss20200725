package cn.besbing.Cron;

import cn.besbing.CommonUtils.MaintainModel.SpringUtil;
import cn.besbing.CommonUtils.MaintainModel.UpdateLimsBillStatus;
import cn.besbing.Entities.Analysis;
import cn.besbing.Entities.NcAnalysisList;
import cn.besbing.Service.Impl.CustomerSqlServiceImpl;
import cn.besbing.Service.Impl.IAnalysisServiceImpl;
import cn.besbing.Service.Impl.INcAnalysisListServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AnalysisThread {

    private Logger logger = LoggerFactory.getLogger(AnalysisThread.class);

    private CustomerSqlServiceImpl customerSqlService = SpringUtil.getBean(CustomerSqlServiceImpl.class);

    private INcAnalysisListServiceImpl iNcAnalysisListService = SpringUtil.getBean(INcAnalysisListServiceImpl.class);

    private IAnalysisServiceImpl iAnalysisService = SpringUtil.getBean(IAnalysisServiceImpl.class);

    /**
     * 获取analysis表中存在、但nc_analysis_list表中不存在的条目,判断依据:analysis.name、analysis.version
     * @return
     */
    public List<Analysis>  getDiffAnalysis() throws Exception {
        Analysis analysis = new Analysis();
        NcAnalysisList ncAnalysisList = new NcAnalysisList();
        List<Analysis> analysisReturnList = new ArrayList<>();
        List<Analysis> analysisAllList = new ArrayList<>();
        try{
            analysisAllList  = iAnalysisService.selectAnalysisByNothing();
        }catch (Exception e){
            logger.error("analysis查询错误");
        }
        if (analysisAllList.size() > 0){
            for (Analysis allanalysislist : analysisAllList){
                ncAnalysisList.setName(allanalysislist.getName());
                ncAnalysisList.setVersion(allanalysislist.getVersion());
                try{
                    ncAnalysisList = iNcAnalysisListService.selectByAnalysisKey(ncAnalysisList);
                    //如果查到，什么也不做，如果查不到立即更新
                    if (ncAnalysisList == null){

                    }
                }catch (Exception e){
                    logger.info("在nc_analysis_list表中查询出错........");
                }
            }
        }else {
            throw new Exception("查询analysis数量小于1");
        }



        return analysisReturnList;
    }

}
