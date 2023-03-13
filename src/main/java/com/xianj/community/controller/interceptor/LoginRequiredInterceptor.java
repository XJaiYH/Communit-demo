package com.xianj.community.controller.interceptor;

import com.xianj.community.annotation.LoginRequired;
import com.xianj.community.util.HostHolder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Host;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    HostHolder hostHolder;
    // 在请求最初判断是否有权限访问
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 拦截的对象应该是方法，要排除静态请求。此处只是定义拦截器，还需要向mvc注册拦截器，注册拦截器在WebMvcConfig中
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);// 检查方法是否有该自定义注解，有则需要拦截
            if(loginRequired != null && hostHolder.getUser() == null){// 该方法需要登录访问，但又没有登录，需要返回到登录页面
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
