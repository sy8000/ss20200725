package cn.besbing.CommonUtils.Utils;

public class MailDTO {

    private String []toUsers ;
    private String context ;

    public String[] getToUsers() {
        return toUsers;
    }

    public void setToUsers(String[] toUsers) {
        this.toUsers = toUsers;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
