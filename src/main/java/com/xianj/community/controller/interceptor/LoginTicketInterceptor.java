package com.xianj.community.controller.interceptor;

import com.xianj.community.entity.LoginTicket;
import com.xianj.community.entity.User;
import com.xianj.community.service.UserService;
import com.xianj.community.util.CookieUtil;
import com.xianj.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;
    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从request中获取cookie有些麻烦，所有封装以下以便复用
        // 从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if(ticket != null){
            // 获取凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                // 如果凭证存在且登录状态有效且有效时间在当前时间之后，认为处于登录状态
                // 根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户，很显然不能简单地存入容器中，因为程序是多线程进行的，这会导致存入的数据可能被别的线程所修改，所以需要对每个线程的私有数据进行隔离保存（每次请求都如此，请求结束应清除）
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    // 取存入的数据，存入魔板引擎
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser", user);
        }
    }

    // 模板引擎执行完后，需要调用clear方法清除线程隔离保存的数据
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
