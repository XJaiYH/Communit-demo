package com.xianj.community.controller;

import com.google.code.kaptcha.Producer;
import com.xianj.community.entity.User;
import com.xianj.community.service.UserService;
import com.xianj.community.util.CommunityConstent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstent {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProduce;
    @Value("server.servlet.context-path")
    private String contextPath;
    @RequestMapping(path="/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path="/login", method = RequestMethod.GET)// 此处两个方法的路径名相同没问题，但是method不能相同，否则冲突了
    // 此处返回的是页面，浏览器所需要的资源以路径的形式封装在页面里面，若浏览器需要这些资源，还需要再次访问服务器申请，所以需要在写一个方法返回资源
    public String getLoginPage(){
        return "/site/login";
    }

    // 返回资源，此处返回验证码图片
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptch(HttpServletResponse response, HttpSession session){// 对于敏感数据，应放在cookie或者session里面，而不能直接返回在html
        // 生成验证码
        String text = kaptchaProduce.createText();
        BufferedImage image = kaptchaProduce.createImage(text);

        // 将验证码存入session
        session.setAttribute("kaptcha", text);

        // 将图片输出给浏览器
        response.setContentType("image/png");// 声明返回类型
        try {
            OutputStream os = response.getOutputStream();// 获取response的输出字节流
            ImageIO.write(image, "png", os);// response由spring MVC维护，所以我们无需关闭
            // 此处意思是 输出image图片，其格式为png，输出流为os
        } catch (IOException e) {
           logger.error(()->"响应验证码失败"+e.getMessage());
        }
    }

    @RequestMapping(path="/register", method = RequestMethod.POST)
    public String register(Model model, User user){// 若网易传入的变量名能够与User对象中的变量名匹配，Spring会将变量的值注入到User对象中
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){// 注册成功后，激活后跳到登录页面
            model.addAttribute("msg", "注册成功，我们已向您的邮箱发送激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");// 跳转的页面
            return "/site/operate-result";
        }
        else{// 注册失败应return到原来的页面，即注册页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activate(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int res = userService.activation(userId, code);
        if(res == ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功，账户以可以正常使用！");
            model.addAttribute("target", "/login");// 跳转的页面
        } else if (res == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "该账户已激活过，请勿重新激活！");
            model.addAttribute("target", "/index");// 跳转的页面
        }else {
            model.addAttribute("msg", "对不起，激活失败！");
            model.addAttribute("target", "/index");// 跳转的页面
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(Model model, User user, String code, boolean rememberMe, HttpSession session, HttpServletResponse response){
        // 生成的验证码之前放到了session中，所以此处需要从session中读取，以判断用户输入的验证码是否正确
        // 登录成功要向用户返回ticket，所以还需要response来返回一个cookie，cookie中存ticket

        // 判断验证码和用户输入的验证码是否相同
        // 验证码和用户输入的code都不应为空
        String kaptcha = (String) session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){// equalsIgnoreCase在比较时忽略大小写
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        // 检查账户、密码是否有问题
        int expiredTime = rememberMe?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(user.getUsername(), user.getPassword(), expiredTime);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());// 初始化ticket的cookie
            cookie.setPath(contextPath);// 设置生效路径，即项目路径
            cookie.setMaxAge(expiredTime);// 设置有效时间
            response.addCookie(cookie);// 返回cookie
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    // logout
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(Model model, @CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }
}
