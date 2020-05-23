package cn.besbing.Conctrollers;

import cn.besbing.CommonUtils.AboutJson.ConverToJson;
import cn.besbing.CommonUtils.MaintainModel.MaintainModelUtils;
import cn.besbing.Dao.CustomerSqlMapper;
import cn.besbing.Entities.InstrumentsWithBLOBs;
import cn.besbing.Entities.Result;
import cn.besbing.Entities.ResultDefKey;
import cn.besbing.Entities.TableTaskFields;
import cn.besbing.Service.Impl.CustomerSqlServiceImpl;
import cn.besbing.Service.Impl.InstrumentsServiceImpl;
import cn.besbing.Service.Impl.TaskInfoServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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

    @Autowired
    InstrumentsServiceImpl instrumentsService;

    @Autowired
    TaskInfoServiceImpl taskInfoService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 登录页跳转
     * @param model
     * @return
     */
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

    /**
     *首页统计数据申请地址及返回
     * @param searchType
     * @return
     */
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
            String s = "";
            //获取日期
            for (int i = 0; i < dateList.size(); i++) {
                s = dateList.get(i).get("TODAY").toString().replace("\"","\'");
                jsonArray.set(i, s);
            }
            //按日期遍历各单
            JSONArray projData = new JSONArray();
            JSONArray taskData = new JSONArray();
            JSONArray startData = new JSONArray();
            JSONArray endData = new JSONArray();
            JSONArray reportData = new JSONArray();
            for (int i = 0;i < jsonArray.size();i++){
                //project
                projData.set(i,Integer.valueOf(customerSqlService.selectOne("select count(1) from qc_commission_h h where " +
                        "substr(h.creationtime,1,10) = '"+ jsonArray.get(i) +"' and dr = 0")));
                //c_proj_task
                taskData.set(i,Integer.valueOf(customerSqlService.selectOne("select count(1) from qc_task_b tb where " +
                        " tb.pk_task_h in (select th.pk_task_h from " +
                        " qc_task_h th where substr(th.creationtime,1,10) = '"+ jsonArray.get(i) + "' and dr = 0)")));
                //au_start_time
                startData.set(i,Integer.valueOf(customerSqlService.selectOne("select count(1) from c_arrangement ca where " +
                        " substr(to_char(ca.acture_start_date,'yyyy-mm-dd'),1,10) = '"+ jsonArray.get(i) + "' " +
                        " and ca.status != 'X'")));
                //au_end_time
                endData.set(i,Integer.valueOf(customerSqlService.selectOne("select count(1) from c_arrangement ca where " +
                        " substr(to_char(ca.acture_end_date,'yyyy-mm-dd'),1,10) = '"+ jsonArray.get(i) + "' " +
                        " and ca.status != 'X'")));
                //report_sign
                reportData.set(i,Integer.valueOf(customerSqlService.selectOne("select count(1) from c_proj_task c where  " +
                        " substr(to_char(c.c_reportsignature_date,'yyyy-mm-dd'),1,10) = '"+ jsonArray.get(i) + "' " +
                        " and c.status = 'T'")));
            }

            //整理json
            String []legendArr = new String[]{"委托单","任务单","开始试验","结束试验","报告签发"};
            jsonObject.put("category",jsonArray);
            jsonObject.put("legend",legendArr);
            jsonObject.put("projData",projData);
            jsonObject.put("taskData",taskData);
            jsonObject.put("startData",startData);
            jsonObject.put("endData",endData);
            jsonObject.put("reportData",reportData);
            //System.out.println(jsonObject);
            return jsonObject;
        }
        return "";
    }


    /**
     * 任务管理页面
     * @return
     */
    @RequestMapping(value = "/task",method = RequestMethod.GET)
    public String task(){
        return "pages/task.html";
    }

    @RequestMapping(value = "/TaskInfo",method = RequestMethod.GET)
    @ResponseBody
    public JSONObject getTaskInfo(){
        List<TableTaskFields> tableTaskFieldsList = taskInfoService.getTask();
        ConverToJson converToJson = new ConverToJson();
        return converToJson.ListToJson(tableTaskFieldsList);
    }

    /**
     * 通过sql_records表查询语句
     * @param 参数 格式{paramtype:init/after,taskid:string}
     * @return json
     */
    @RequestMapping(value = "/getParameters",method = RequestMethod.POST)
    @ResponseBody
    public JSONObject getTestParameters(String type,String taskid){
        JSONObject jsonObject = new JSONObject();
        String sql = "";
        if ("init".equals(type)){
            sql = customerSqlService.selectOne("select sql_text from sql_records where sql_code = 'init' ");
            sql = sql.replace("sheny",taskid.substring(0,13));
            //List<Map<String, Object>> maps = customerSqlService.selectList(sql);
            ConverToJson converToJson = new ConverToJson();
            jsonObject = converToJson.ListToJson(customerSqlService.selectList(sql));
        }else {
            sql = customerSqlService.selectOne("select sql_text from sql_records where sql_code = 'after' ");
            sql = sql.replace("sheny",taskid);
            //List<Map<String, Object>> maps = customerSqlService.selectList(sql);
            ConverToJson converToJson = new ConverToJson();
            jsonObject = converToJson.ListToJson(customerSqlService.selectList(sql));
        }
        return jsonObject;
    }

    /**
     * 接收传的josn，自动写参数到result
     * 传入json格式
     * {
     *     type:init/after
     *     data:[{...},{...}]
     * }
     */
    @RequestMapping(value = "/upLoadParameters",method = RequestMethod.POST)
    @ResponseBody
    public JSONObject writeResult(String jsonStr){
        //JSON.toJSONString(Result.class);
        JSONArray jsonArray = JSONArray.parseArray(jsonStr);

        return new JSONObject();
    }

    /**
     * 仪器管理跳转
     * @param model
     * @return
     */
    @RequestMapping(value = "/instruments",method = RequestMethod.GET)
    public Object getInstruments(Model model){
        List<InstrumentsWithBLOBs>  instruments = instrumentsService.allInstruments();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",0);
        jsonObject.put("msg","");
        jsonObject.put("count",instrumentsService.countByExample(null));
        jsonObject.put("data",JSON.toJSON(instruments));
        model.addAttribute("instrument",jsonObject);
        return "pages/instruments";
    }

    @RequestMapping(value = "/getinstruments",method = RequestMethod.GET)
    @ResponseBody
    public Object getInstruments(){
        List<InstrumentsWithBLOBs>  instruments = instrumentsService.allInstruments();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",0);
        jsonObject.put("msg","");
        jsonObject.put("count",instrumentsService.countByExample(null));
        jsonObject.put("data",JSON.toJSON(instruments));

        return jsonObject;
    }

    /**
     *
     * @param obj  微服务端传入的参数，格式为"rowid_taskid"，用split切分后，去查询，如果有则返回试验名称和样品名称，没有则直接返回错误
     * @return
     */
    @RequestMapping(value = "/ValidReports",method = RequestMethod.GET)
    @ResponseBody
    public Object getReportValid(Object obj){
        logger.info(obj.toString());
        String returnValue = "";
        String unionString = obj.toString();
        String[] param = unionString.split("_");
        String sql = "select 1 from c_porj_task where rowid = '" + param[0] + "' and task_id = '" + param[1] + "'";
        String result =  customerSqlService.selectOne(sql);
        if (result != null){
            returnValue = "报告无误";
        }else{
            returnValue = "报告非检测中心出具";
        }
        return returnValue;
    }

}
