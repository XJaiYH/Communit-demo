package com.xianj.community;

import com.xianj.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 使用main里面类的配置
public class SensitiveTest {

    @Autowired
    SensitiveFilter filter;

    @Test
    public void testSensitiveFilter(){
        String text = "赌**博 **吸*毒**色***情";
        text = filter.filter(text);
        System.out.println(text);
    }


}
