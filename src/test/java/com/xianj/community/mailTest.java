package com.xianj.community;

import com.xianj.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.Thymeleaf;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 使用main里面类的配置
public class mailTest {
    @Autowired
    public MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testSendMail(){
        mailClient.sendEmail("18710051699@163.com", "test", "just a test");
    }

    @Test
    public void testHtmlSend(){
        Context context = new Context();
        context.setVariable("username", "xianjun");
        String content = templateEngine.process("mail/demo", context);
        System.out.println(content);
        mailClient.sendEmail("18710051699@163.com", "test", content);
    }
}
