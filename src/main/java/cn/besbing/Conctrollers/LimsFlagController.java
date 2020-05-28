package cn.besbing.Conctrollers;

import cn.besbing.CommonUtils.MaintainModel.UpdateLimsBillStatus;
import cn.besbing.Service.Impl.CustomerSqlServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/lims")
public class LimsFlagController {

    Logger logger = LoggerFactory.getLogger(LimsFlagController.class);


    @Autowired
    CustomerSqlServiceImpl customerSqlService;

    //@Transactional(rollbackFor = Exception.class)
    @RequestMapping(value = "/updateToSign",method = RequestMethod.POST)
    @ResponseBody
    public String updateToSign(@RequestBody String taskIds){
        JSONArray jsonArray = JSONArray.parseArray(taskIds);
        JSONObject jsonObject = new JSONObject();
        UpdateLimsBillStatus updateLimsBillStatus = new UpdateLimsBillStatus();
        List<String> projList = new ArrayList<String>();
        for (int i=0;i<jsonArray.size();i++){
            jsonObject = jsonArray.getJSONObject(i);
            projList.add(jsonObject.get("project").toString());
        }
        return String.valueOf(updateLimsBillStatus.moveToSampleMgr(projList));
    }

    /**
     * 计划排程跳转
     * @param taskIds
     * @return
     */
    @RequestMapping(value = "/updateToPlan",method = RequestMethod.POST)
    @ResponseBody
    public String updateToPlan(@RequestBody String taskIds){
        JSONArray jsonArray = JSONArray.parseArray(taskIds);
        JSONObject jsonObject = new JSONObject();
        UpdateLimsBillStatus updateLimsBillStatus = new UpdateLimsBillStatus();
        List<String> projList = new ArrayList<String>();
        for (int i=0;i<jsonArray.size();i++){
            jsonObject = jsonArray.getJSONObject(i);
            projList.add(jsonObject.get("project").toString());
        }
        return String.valueOf(updateLimsBillStatus.moveToPlan(projList));
    }

    /**
     * 任务分配跳转
     * @param taskIds
     * @return
     */
    @RequestMapping(value = "/updateToBistribution",method = RequestMethod.POST)
    @ResponseBody
    public String updateToBistribution(@RequestBody String taskIds){
        JSONArray jsonArray = JSONArray.parseArray(taskIds);
        JSONObject jsonObject = new JSONObject();
        UpdateLimsBillStatus updateLimsBillStatus = new UpdateLimsBillStatus();
        List<String> taskidList = new ArrayList<String>();
        for (int i=0;i<jsonArray.size();i++){
            jsonObject = jsonArray.getJSONObject(i);
            taskidList.add(jsonObject.get("taskId").toString());
        }
        return String.valueOf(updateLimsBillStatus.moveToLimsStempGenaral(taskidList,"distribution"));
    }


    /**
     * 试验中跳转
     * @param taskIds
     * @return
     */
    @RequestMapping(value = "/updateToTesting",method = RequestMethod.POST)
    @ResponseBody
    public String updateToTesting(@RequestBody String taskIds){
        JSONArray jsonArray = JSONArray.parseArray(taskIds);
        JSONObject jsonObject = new JSONObject();
        UpdateLimsBillStatus updateLimsBillStatus = new UpdateLimsBillStatus();
        List<String> taskidList = new ArrayList<String>();
        for (int i=0;i<jsonArray.size();i++){
            jsonObject = jsonArray.getJSONObject(i);
            taskidList.add(jsonObject.get("taskId").toString());
        }
        return String.valueOf(updateLimsBillStatus.moveToLimsStempGenaral(taskidList,"testing"));
    }

    /**
     * 工程师跳转
     * @param taskIds
     * @return
     */
    @RequestMapping(value = "/updateToEngineer",method = RequestMethod.POST)
    @ResponseBody
    public String updateToEngineer(@RequestBody String taskIds){
        JSONArray jsonArray = JSONArray.parseArray(taskIds);
        JSONObject jsonObject = new JSONObject();
        UpdateLimsBillStatus updateLimsBillStatus = new UpdateLimsBillStatus();
        List<String> taskidList = new ArrayList<String>();
        for (int i=0;i<jsonArray.size();i++){
            jsonObject = jsonArray.getJSONObject(i);
            taskidList.add(jsonObject.get("taskId").toString());
        }
        return String.valueOf(updateLimsBillStatus.moveToLimsStempGenaral(taskidList,"engineer"));
    }

    /**
     * 签发跳转
     * @param taskIds
     * @return
     */
    @RequestMapping(value = "/updateToReport",method = RequestMethod.POST)
    @ResponseBody
    public String updateToReport(@RequestBody String taskIds){
        JSONArray jsonArray = JSONArray.parseArray(taskIds);
        JSONObject jsonObject = new JSONObject();
        UpdateLimsBillStatus updateLimsBillStatus = new UpdateLimsBillStatus();
        List<String> taskidList = new ArrayList<String>();
        for (int i=0;i<jsonArray.size();i++){
            jsonObject = jsonArray.getJSONObject(i);
            taskidList.add(jsonObject.get("taskId").toString());
        }
        return String.valueOf(updateLimsBillStatus.moveToLimsStempGenaral(taskidList,"report"));
    }

    /**
     * 任务取消跳转
     * @param taskIds
     * @return
     */
    @RequestMapping(value = "/updateToTaskend",method = RequestMethod.POST)
    @ResponseBody
    public String updateToTaskend(@RequestBody String taskIds){
        int flag = 0;
        JSONArray jsonArray = JSONArray.parseArray(taskIds);
        JSONObject jsonObject = new JSONObject();
        //UpdateLimsBillStatus updateLimsBillStatus = new UpdateLimsBillStatus();
        //List<String> taskidList = new ArrayList<String>();
        for (int i=0;i<jsonArray.size();i++){
            jsonObject = jsonArray.getJSONObject(i);
            //taskidList.add(jsonObject.get("taskId").toString());
            flag += customerSqlService.update("update c_proj_task set status = 'F' where task_id = '"+ jsonObject.get("taskId").toString() +"' ");
        }
        return String.valueOf(flag);
    }







}
