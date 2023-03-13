package com.xianj.community.util;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;
    // 发送和接受邮件的人是谁，此处我们的发送人是固定的，在属性文件中配置了
    // 发送的内容是什么
    @Value("${spring.mail.username}")
    private String from;

    public void sendEmail(String to, String subject, String content){
        try {
            MimeMessage mimeMailMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMailMessage);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);// 加了True这个参数，表示允许支持html文本，如何使用thymeleaf发送html邮件？首先做邮件魔板
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error(()->"邮件发送失败" + e.getMessage());
        }
    }

}
