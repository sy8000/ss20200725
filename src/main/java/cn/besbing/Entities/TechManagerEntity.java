package cn.besbing.Entities;

public class TechManagerEntity {


    private String billno;

    private String billname;

    private String projtype;

    private String creator;

    @Override
    public String toString() {
        return "TechManagerEntity{" +
                "billno='" + billno + '\'' +
                ", billname='" + billname + '\'' +
                ", projtype='" + projtype + '\'' +
                ", creator='" + creator + '\'' +
                '}';
    }

    public String getBillno() {
        return billno;
    }

    public void setBillno(String billno) {
        this.billno = billno;
    }

    public String getBillname() {
        return billname;
    }

    public void setBillname(String billname) {
        this.billname = billname;
    }

    public String getProjtype() {
        return projtype;
    }

    public void setProjtype(String projtype) {
        this.projtype = projtype;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
