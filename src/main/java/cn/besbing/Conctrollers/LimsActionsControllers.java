package cn.besbing.Conctrollers;


import cn.besbing.CommonUtils.AboutJson.ConverToJson;
import cn.besbing.CommonUtils.Utils.MailDTO;
import cn.besbing.Entities.*;
import cn.besbing.Service.Impl.*;
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

    @Autowired
    IQcCommissionHServiceImpl iQcCommissionHService;

    @Autowired
    ICommissionBServiceImpl iCommissionBService;

    @Autowired
    IQcCommissionRServiceImpl iQcCommissionRService;
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
        List<QcCommissionB> qcCommissionBList = null;
        QcCommissionC qcCommissionC = null;
        List<QcCommissionR> qcCommissionRList = null;
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
        //委托单：头、体、修改、试验前;任务单：头、体、试验后、条件
        String commssionHPk = "";
        List<String> commssionBPkList = new ArrayList<>();
        List<String> commssionCPkList = new ArrayList<>();  //没j8用
        List<String> commssionRPkList = new ArrayList<>();
        String taskHPk = "";
        List<String> taskBPk = new ArrayList<>();
        List<String> taskRPk = new ArrayList<>();
        List<String> taskSPk = new ArrayList<>();
        /**
         * 业务流程开始处理
         * 说明：先得到8个nc单的pk，然后获取原lims的样板数据，替换后写入
         */
        try{
            logger.info("......开始转换NC中单据primary..........");

            commssionHPk = customerSqlService.selectOne("select PK_COMMISSION_H from QC_COMMISSION_H where " +
                    "pk_COMMISSION_H = '" + json.get("pkcommissionh") + "' and dr = 0");
            commssionBPkList = customerSqlService.selectAsList("select PK_COMMISSION_B from QC_COMMISSION_H where PK_COMMISSION_H = " +
                    "'" + commssionHPk + "' and dr = 0");
            commssionRPkList = customerSqlService.selectAsList("select PK_COMMISSION_R from QC_COMMISSION_R where PK_COMMISSION_B = '" + commssionBPkList + "' and dr = 0") ;

            taskHPk = customerSqlService.selectOne("select PK_TASK_H from QC_TASK_H where pk_commission_h = '" + commssionHPk + "' and dr = 0");

            taskBPk = customerSqlService.selectAsList("select PK_TASK_B from QC_TASH_B where pk_task_h = '"+ taskHPk +"' and dr = 0");

            taskRPk = customerSqlService.selectAsList("select PK_TASK_R from QC_TASK_R where pk_task_b = '" + taskBPk + "' and dr = 0");

            taskSPk = customerSqlService.selectAsList("select PK_TASK_S from QC_TASK_S where pk_task_b = '" + taskBPk + "' and dr = 0");

            logger.info("......NC中单据primary获取完成..........");
        }catch(Exception e){
            logger.error("......NC中单据primary获取出错，错误：{}..........",e.getStackTrace());
            throw new Exception();
        }
        try{
            logger.info("........开始获取NC中各表头表体的POJO原始数据...................");
            qcCommissionH = iQcCommissionHService.selectQMHByPrimary(commssionHPk);
            qcCommissionBList = iCommissionBService.getCommissionBodyList(commssionBPkList);
            qcCommissionRList = iQcCommissionRService.getCommissionRList(commssionRPkList);
        }catch (Exception e){

        }


        return converToJson.ListToJson(list);
    }



}
