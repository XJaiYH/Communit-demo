package com.xianj.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

// 配置类
@Configuration // 表示为配置类
public class AlphaConfig {
    @Bean // 意义是，该方法返回的对象将被装配到容器中
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
