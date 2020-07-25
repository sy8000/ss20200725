package cn.besbing.CommonUtils.Utils;


import cn.besbing.CommonUtils.MaintainModel.SpringUtil;
import cn.besbing.Service.Impl.CustomerSqlServiceImpl;

import java.util.List;

/**
 * 工具类：
 * 主要功能为获取NC内各单的pk及list<pk>
 */
public class GetNcPrimaryKey {

    private CustomerSqlServiceImpl customerSqlService = SpringUtil.getBean(CustomerSqlServiceImpl.class);

    //取NC委托单表头主键
    public String getCommissionHPrimaryKey(String billno){
        return customerSqlService.selectOne("select pk_commission_h from qc_commission_h where " +
                "billno = '" + billno + "' and dr = 0");
    }

    //取NC委托单表体主键
    public List<String> getCommissionBPrimaryKey(String billno){
        return customerSqlService.selectAsList("select b.pk_commission_b from qc_commission_b b where b.dr = 0 " +
                " and b.pk_commission_h in (select h.pk_commission_h from qc_commission_h h where h.billno = '"+ billno +"' and h.dr = 0)");
    }

    //取委托单孙表试验前主键
    public List<String> getCommissionRPrimaryKey(String billno){
        return customerSqlService.selectAsList("select r.pk_commission_r from qc_commission_r r where r.pk_commission_b in (" +
                "select b.pk_commission_b from qc_commission_b b where b.dr = 0 and b.pk_commission_h in (select h.pk_commission_h " +
                " from qc_commission_h h where h.billno = '" + billno + "' and h.dr = 0)) and r.dr = 0");
    }

    //取任务单表头主键
    public String getTaskHPrimaryKey(String billno){
        return customerSqlService.selectOne("select pk_task_h  from qc_task_h where dr = 0 " +
                " and pk_commission_h = '" + getCommissionHPrimaryKey(billno) + "'");
    }

    //取任务单表体主键
    public List<String> getTaskBPrimaryKey(String billno){
        return customerSqlService.selectAsList("select pk_task_b from qc_task_b where dr = 0 " +
                "and pk_task_h = '" + getTaskHPrimaryKey(billno) + "'");
    }

    //取任务单孙表测试条件
    public List<String> getTaskSPrimaryKey(String billno){
        return customerSqlService.selectAsList("select s.pk_task_s from qc_task_s s where s.dr = 0 and s.pk_task_b in " +
                " (select b.pk_task_b from qc_task_b b where b.dr = 0 and b.pk_task_h = '" + getTaskHPrimaryKey(billno) + "')");
    }

    //取任务单孙表试验后参数
    public List<String> getTaskRPrimaryKey(String billno){
        return customerSqlService.selectAsList("select r.pk_task_r from qc_task_r r where r.dr = 0 and r.pk_task_b in " +
                " (select b.pk_task_b  from qc_task_b b where b.dr = 0 and b.pk_task_h = '" + getTaskHPrimaryKey(billno) + "')");
    }

    //委托单类型
    public String getProjectType(String projectTypePrimaryKey){
        return customerSqlService.selectOne("select trim(name) from nc_proj_type where trim(pk_proj_type) = '" + projectTypePrimaryKey + "'");
    }

    //所属公司、付费单位
    public String getCompany(String companyPrimaryKey){
        return customerSqlService.selectOne("select name from org_orgs where pk_org = '" + companyPrimaryKey + "'");
    }

    //查人员
    public String getSmuser(String cuserid){
        return customerSqlService.selectOne("select user_name from sm_user where cuserid = '" + cuserid + "'");
    }

    //样品总数
    public String getSampleTotal(String taskHPrimaryKey){
        return customerSqlService.selectOne("select sum(quantity) from qc_commission_b where pk_commission_h = '" + taskHPrimaryKey + "'");
    }


    //产品大类
    public String getProductType(String productType){
        return customerSqlService.selectOne("select trim(prod_type) from nc_first_type where pk_first_type = '" + productType + "'");
    }


    //试验后样品处理
    public String getSampleAfterTest(String sampleAfterTestPrimaryKey){
        return customerSqlService.selectOne("select trim(nc_ratain_name) from nc_ratain_handle where pk_ratain_handle = '" + sampleAfterTestPrimaryKey + "'");
    }

    //报告语言
    public String getReportLanguage(String reportLangPrimaryKey){
        return customerSqlService.selectOne("select lis_name from nc_report_lang where pk_report_lang='" + reportLangPrimaryKey  + "'");
    }

    //报告格式
    public String getReportFormat(String reportFormatPrimaryKey){
        return customerSqlService.selectOne("select trim(rp_report_name) from nc_report_type where pk_report_type = '" + reportFormatPrimaryKey + "'");
    }

}
