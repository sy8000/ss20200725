package cn.besbing.Conctrollers;

import cn.besbing.CommonUtils.MaintainModel.MaintainModelUtils;
import cn.besbing.Dao.CustomerSqlMapper;
import cn.besbing.Service.Impl.CustomerSqlServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CustomerSqlController {

    @Autowired
    CustomerSqlServiceImpl customerSqlService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/welcomemain",method = RequestMethod.GET)
    public String welcommain(Model model){
        logger.info("************开始加载maintain的页面信息********************");
        /*MaintainModelUtils maintainModelUtils = new MaintainModelUtils();
        model = maintainModelUtils.getMaintainUIData();*/
        //客户累计
        String creatorsSql = "select count(1) from (select distinct h.creator from qc_commission_h h where h.dr = 0)";
        String creatorsNum = customerSqlService.selectOne(creatorsSql);
        model.addAttribute("creators",creatorsNum);
        //web端委托单累计
        String webProjSql = "select count(1) from qc_commission_h h where h.dr = 0";
        String webProjNum = customerSqlService.selectOne(webProjSql);
        model.addAttribute("webProjNum",webProjNum);
        //web端任务单累计
        String webTaskSql = "select count(1) from qc_task_b b where b.dr = 0";
        String webTaskNum = customerSqlService.selectOne(webTaskSql);
        model.addAttribute("webTaskNum",webTaskNum);
        //labware传入统计
        String lwProjSql = "select count(1) from project p where p.ts is not null and p.closed = 'F' ";
        String lwTaskSql = "select count(1) from c_proj_task cpt where cpt.project in ";
        lwTaskSql += " (select p.name from project p where p.ts is not null and p.closed = 'F') " +
                "and upper(cpt.status) != 'X' ";

        String lwProjAndTaskNum = customerSqlService.selectOne(lwProjSql) + "/" + customerSqlService.selectOne(lwTaskSql);
        model.addAttribute("lwProjAndTaskNum",lwProjAndTaskNum);

        //统计sql
        /*String countSql = "select vdef1 from report_path where pk_report_path = '78901234567890123457'";
        String basicCountSql = customerSqlService.selectOne(countSql);
        String projCountSql = basicCountSql.replace("field","u.creationtime");
        projCountSql = projCountSql.replace("tablename","qc_commission_h u");
        List<Map<String, Object>> projList = customerSqlService.selectList(projCountSql);
        model.addAttribute("projList",projList);*/


        logger.info("************加载maintain的页面信息结束********************");
        return "pages/welcomemain";
    }

    @RequestMapping(value = "/TongjiReport",method = RequestMethod.POST)
    @ResponseBody
    public Object getTongjiReport(@Param("searchType")String searchType){
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if ("date".equals(searchType)) {
            //执行日期查询
            List<Map<String, Object>> dateList = customerSqlService.
                    selectList("select to_char (sysdate- level + 1, 'yyyy-mm-dd') today FROM DUAL connect BY " +
                            "LEVEL <= 7 order by today asc");
            System.out.println("**************************");
            for (int i = 0; i < dateList.size(); i++) {
                jsonArray.set(i, dateList.get(i).get("TODAY"));
            }
            System.out.println(jsonArray);
            return jsonArray;
        }
        return "";
    }

}
