package cn.besbing.Conctrollers;


import cn.besbing.Entities.AnalysisTable;
import cn.besbing.Service.Impl.CustomerSqlServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/**
 * 此类做为dataloader cloud中简化表格的控制器，如分析模板更新等
 */

@Controller
public class TableController {

    String analysisDataSql = "select name,version,active,analysis_type,description,to_char(changed_on,'yyyy-mm-dd') changed_on,changed_by,removed," +
            " t_analysis_method,c_method_chapter,c_test_type,c_allowed_prod_type from analysis where removed = 'F' " +
            " order by name asc";

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    CustomerSqlServiceImpl customerSqlService;


    @RequestMapping(value = "/analysis",method = RequestMethod.GET)
    public String welcommain(Model model){
        logger.info("************加载analysisTable的页面信息结束********************");
        return "pages/analysistable";
    }

    @RequestMapping(value = "/getAnalysisDataForTable",method = RequestMethod.GET)
    @ResponseBody
    public JSONObject getAnalysisTable(@RequestParam("page") Integer page, @RequestParam("limit") Integer limit, @RequestParam(value = "keyword", required = false) String keyword){

        /*String analysisDataSql = "select * from " +
                "(select ROW_.*,ROWNUM ROWNUM_ from " +
                "(select " +
                "name," +
                "version," +
                "active," +
                "analysis_type," +
                "description," +
                "changed_on," +
                "changed_by,removed, t_analysis_method,c_method_chapter,c_test_type,c_allowed_prod_type" +
                " from analysis where removed = 'F'  order by name asc ) ROW_" +
                " where ROWNUM <= maxnum)" +
                " where ROWNUM_ >= minux";*/

        JSONObject jsonObject = new JSONObject();
        try {
            if (null == page) {
                page = 1;
            }
            if (null == limit) {
                limit = 10;
            }
            /*int startIndex = page * limit;
            int endIndex = (page + 1) * limit;
            analysisDataSql = analysisDataSql.replace("minux",String.valueOf(startIndex));
            analysisDataSql = analysisDataSql.replace("maxnum",String.valueOf(endIndex));*/
            List<AnalysisTable> analysisDatas = customerSqlService.selectAsAnalysisTable(analysisDataSql);
            jsonObject.put("code",0);
            jsonObject.put("msg","");
            jsonObject.put("count",analysisDatas.size());
            jsonObject.put("data",  JSON.toJSON(analysisDatas)  );

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
