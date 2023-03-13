package com.xianj.community.config;

import com.xianj.community.controller.interceptor.AlphaInterceptor;
import com.xianj.community.controller.interceptor.LoginRequiredInterceptor;
import com.xianj.community.controller.interceptor.LoginTicketInterceptor;
import com.xianj.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {// 要求实现一个接口，配置拦截器
    @Autowired
    private AlphaInterceptor alphaInterceptor;// 这是我们定义的拦截器
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;
    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;
    @Autowired
    private MessageInterceptor messageInterceptor;
    // 注册接口
    @Override
    public void addInterceptors(InterceptorRegistry registry) {// 如果不写路径，则会拦截所有路径
        registry.addInterceptor(alphaInterceptor).excludePathPatterns(// 说明不用拦截哪些路径
                "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg"// 要排除静态资源
        ).addPathPatterns("/register", "/login");// 表示要拦截的路径  (这只是测试)

        registry.addInterceptor(loginTicketInterceptor).excludePathPatterns(// 说明不用拦截哪些路径
                "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg"// 要排除静态资源
        );

//        registry.addInterceptor(loginRequiredInterceptor).excludePathPatterns(// 说明不用拦截哪些路径
//                "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg"// 要排除静态资源
//        );

        registry.addInterceptor(messageInterceptor).excludePathPatterns(// 说明不用拦截哪些路径
                "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg"// 要排除静态资源
        );
    }
}
