package com.xianj.community.controller;

import com.xianj.community.entity.User;
import com.xianj.community.service.UserService;
import com.xianj.community.util.CommunityUtil;
import com.xianj.community.util.HostHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.upload}")
    private String uploadPath;
    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    // 更新头像以及路径
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {// 如果传入的只有一个文件，声明一个。如果是多个文件， 则声明一个数组
        if (headerImage == null) {
            model.addAttribute("fileError", "还未选择文件！");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();// 用户传入的原始文件名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("fileError", "文件格式错误");
        }
        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error(() -> "上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }
        // 如果存储成功，需要更新当前用户头像的路径（web访问路径）
        // http://localhost/community/userHeader/xxx.png  大致是这样
        User user = hostHolder.getUser();// 需要当前用户是谁吧
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";// 返回重定向，其中有包含新头像的地址，浏览器再次访问该地址获取新头像
    }

    // 获取用户上传的文件，在本地服务器中，路径为uploadPath + “/” + fileName
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {// 该方法向浏览器响应一个图片，即二进制字符串，所以通过流进行输出，在方法里手动调用response返回
        fileName = uploadPath + "/" + fileName;
        // 输出的是图片，应先声明输出文件的格式，所以需要解析文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        // 图片是二进制进行传输，所以需要获取字节流
        try (
                OutputStream os = response.getOutputStream();// 获取response的输出字节流
                FileInputStream fis = new FileInputStream(fileName);// 从本地读取图像，字节流形式
        ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer, 0, b);
            }

        } catch (IOException e) {
            logger.error(()->"读取头像失败" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(path = "/uploadPsd", method = RequestMethod.POST)
    public String uploadPassword(Model model, String oldPassword, String newPassword, String checkNewPassword){
        Map<String, Object> map = userService.updatePassword(oldPassword, newPassword, checkNewPassword);
        if(map == null || map.isEmpty()){// 说明更新成功
            return "redirect:/logout";
        }
        else{
            model.addAttribute("passwordOldMsg", map.get("passwordOldMsg"));
            model.addAttribute("passwordNewMsg", map.get("passwordNewMsg"));
            model.addAttribute("passwordCheckMsg", map.get("passwordCheckMsg"));
            return "/site/setting";
        }
    }

}
