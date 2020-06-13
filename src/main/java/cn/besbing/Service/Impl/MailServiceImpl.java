package cn.besbing.Service.Impl;

import cn.besbing.CommonUtils.Utils.MailDTO;
import cn.besbing.Service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;


@Service
public class MailServiceImpl implements MailService {

    @Autowired(required = false)
    MailSender mailSender;

    public void sendMail(MailDTO mailDTO){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("Trial progress update notification from DataLoader Cloud service lims3.0");
        mailMessage.setText("Lims3.0 Auto Send");
        mailMessage.setFrom("lab@hongfa.cn");
        mailMessage.setTo("1002437@hongfa.cn","1000514@hongfa.cn");
        mailSender.send(mailMessage);
    }



}
