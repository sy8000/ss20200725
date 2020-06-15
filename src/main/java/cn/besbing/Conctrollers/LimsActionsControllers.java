package cn.besbing.Conctrollers;


import cn.besbing.CommonUtils.AboutJson.ConverToJson;
import cn.besbing.CommonUtils.Utils.MailDTO;
import cn.besbing.Entities.*;
import cn.besbing.Service.Impl.CustomerSqlServiceImpl;
import cn.besbing.Service.Impl.IQcTaskHServiceImpl;
import cn.besbing.Service.Impl.MailServiceImpl;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/lims")
public class LimsActionsControllers {


    Logger logger = LoggerFactory.getLogger(LimsActionsControllers.class);


    @Autowired
    CustomerSqlServiceImpl customerSqlService;

    @Autowired
    IQcTaskHServiceImpl iQcTaskHService;

    @Autowired
    MailServiceImpl mailService;


    /**
     * 技术主管驳回
     * @param json
     * @return
     */
    @RequestMapping(value = "/TechReject",method = RequestMethod.POST)
    @ResponseBody
    public JSONObject approvalRejectByTechManager(@RequestBody JSONObject json) throws Exception {
        List<String> list = new ArrayList<>();
        ConverToJson converToJson = new ConverToJson();
        QcTaskH qcTaskH = null;
        String taskPk = "";
        String creatorMail = "";
        int updateflag = -1;
        //获取qctaskh主键
        try{
            taskPk = customerSqlService.selectOne("select trim(PK_TASK_H) from QC_TASK_H where " +
                    "pk_COMMISSION_H = '" + json.get("pkcommissionh") + "' and dr = 0");
        }catch (Exception e){
            logger.error("获取任务单主键出错，错误原因：{}",e.getStackTrace());
            throw new Exception("获取任务单主键出错，错误原因：" + e.getStackTrace());
        }
        //更新单据审批状态
        try{
            qcTaskH = iQcTaskHService.selectTaskHByPk(taskPk);
            qcTaskH.setApprovedate("");
            qcTaskH.setApprovenote(json.get("reject").toString());
            qcTaskH.setApprover("");
            qcTaskH.setApprovestatus(BigDecimal.valueOf(-1));
            updateflag = iQcTaskHService.updateTaskHB(qcTaskH);
        }catch (Exception e){
            logger.error("更新任务单状态出错，错误原因：{}",e.getStackTrace());
            throw new Exception("更新任务单状态出错，错误原因：" + e.getStackTrace());
        }
        //给制单人发邮件
        try {
            MailDTO mailDTO = new MailDTO();
            creatorMail = customerSqlService.selectOne(" select trim(user_code) || '@hongfa.cn' from sm_user  " +
                    "where user_name = '" + json.get("creator").toString().trim() + "'");
            mailDTO.setSubject("关于委托单:"+ json.get("billno") +"被退回的通知");
            mailDTO.setContext("尊敬的用户：" + json.get("creator").toString() + "您好： 很抱歉的通知您，您单号为" +
                    json.get("billno").toString() + "的委托单目前已被技术主管退回，退回原因：" + json.get("reject").toString() +
                    "。请尽快重新登录:http://10.0.11.82 或 http://limsweb.hongfa.cn  修订委托单，以保证流程正常运转。谢谢合作！");
            mailDTO.setToUsers(new String[]{creatorMail});
            mailService.sendMail(mailDTO);
        }catch (Exception e){
            logger.error("更新任务单发送邮件出错，错误原因：{}",e.getStackTrace());
            throw new Exception("更新任务单发送邮件出错，错误原因：" + e.getStackTrace());
        }
        list.add("success");
        return converToJson.ListToJson(list);
    }




    /**
     * 技术主管审批
     * @param json
     * @return
     */
    @RequestMapping(value = "/TechApproval",method = RequestMethod.POST)
    @ResponseBody
    public JSONObject approvalPassByTechManager(@RequestBody JSONObject json) throws Exception {
        List<String> list = new ArrayList<>();
        ConverToJson converToJson = new ConverToJson();
        QcCommissionH qcCommissionH = null;
        QcCommissionB qcCommissionB = null;
        QcCommissionC qcCommissionC = null;
        QcCommissionR qcCommissionR = null;
        QcTaskH qcTaskH = null;
        QcTaskB qcTaskB = null;
        QcTaskR qcTaskR = null;
        QcTaskS qcTaskS = null;
        Project project = null;
        CProjTask cProjTask = null;
        CProjLoginSample cProjLoginSample = null;
        Test test = null;
        Sample sample = null;
        Result result = null;
        CProjParaA cProjParaA = null;
        CProjTaskParaB cProjTaskParaB = null;

        return converToJson.ListToJson(list);
    }



}
