package cn.besbing.Service.Impl;

import cn.besbing.CommonUtils.Utils.GetNcPrimaryKey;
import cn.besbing.Entities.CProjLoginSample;
import cn.besbing.Entities.CProjTask;
import cn.besbing.Entities.Project;
import cn.besbing.Entities.QcCommissionH;
import cn.besbing.Service.IWriteBackToLims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;


@Service
public class IWriteBackToLimsImpl implements IWriteBackToLims {

    @Autowired(required = false)
    IProjectServiceImpl iProjectService;

    @Autowired(required = false)
    ICprojLoginSampleServiceImpl iCprojLoginSampleService;

    @Autowired(required = false)
    IQcCommissionHServiceImpl iQcCommissionHService;

    @Autowired(required = false)
    CustomerSqlServiceImpl customerSqlService;

    GetNcPrimaryKey ncPrimaryKey = new GetNcPrimaryKey();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    Project templateProject = null;
    CProjLoginSample templateCProjLoginSample = null;


    public synchronized String WriteBackToLims(String projectBillNumber){
        String returnMsg = "";
        try{
            /**
             * 首先获取模板project，然后调用数据清洗方法，返回清洗完成的project，执行insert
             */
            //templateProject = iProjectService.getLimsExampleProject();
            Project project = projectETL(projectBillNumber);
            if (project != null){
                //写入project

            }else {
                return "NC委托单获取失败";
            }
        }catch (Exception e){
            return e.getStackTrace().toString();
        }
        return returnMsg;
    }

    private Project projectETL(String projectBillNumber) {
        Project project = null;
        QcCommissionH qcCommissionH = null;
        try{
            project = iProjectService.getLimsExampleProject();
            qcCommissionH = iQcCommissionHService.selectQMHByPrimary(ncPrimaryKey.getCommissionHPrimaryKey(projectBillNumber));
            project.setClosed("F");
            project.setName(qcCommissionH.getBillno());
            project.setTemplate(ncPrimaryKey.getProjectType(qcCommissionH.getPkCommissiontype()));
            project.setTitle(qcCommissionH.getBillname());
            project.setDescription(qcCommissionH.getProgressneed());
            project.setDateCreated(sdf.parse(qcCommissionH.getCreationtime()));
            project.setDateReceived(sdf.parse(qcCommissionH.getLastmaketime()));
            project.setDateUpdated(sdf.parse(qcCommissionH.getLastmaketime()));
            project.setCustomer(ncPrimaryKey.getCompany(qcCommissionH.getPkOwner()));
            project.setCustomerContact(qcCommissionH.getContract());
            project.setOwner(ncPrimaryKey.getSmuser(qcCommissionH.getCuserid()));
            project.setCreatedBy(ncPrimaryKey.getSmuser(qcCommissionH.getCuserid()));
            project.setNumSamples(Long.valueOf(ncPrimaryKey.getSampleTotal(ncPrimaryKey.getTaskHPrimaryKey(projectBillNumber))));
            project.settSourceCustomer(ncPrimaryKey.getCompany(qcCommissionH.getPkPayer()));
            project.setcPhoneNumber(qcCommissionH.getTeleno());
            project.setcEmailAddress(qcCommissionH.getEmail());
            project.setcProductType(ncPrimaryKey.getProductType(qcCommissionH.getPkMaincategory()));
            project.setcApplyType(ncPrimaryKey.getProjectType(qcCommissionH.getPkCommissiontype()));
            project.setcRetainHandle(ncPrimaryKey.getSampleAfterTest(qcCommissionH.getSampledealtype()));
            project.setcCoaLanguage(ncPrimaryKey.getReportLanguage(qcCommissionH.getReportlang()));
            project.setcCoaFormat(ncPrimaryKey.getReportFormat(qcCommissionH.getReportformat()));
            //开始从c_test_purpose依次赋值

        }catch (Exception e){
            e.printStackTrace();
        }
        return project;
    }


}
