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
        return String.valueOf(updateLimsBillStatus.moveToSampleMgr(projList,customerSqlService));
    }



}
