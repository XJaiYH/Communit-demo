package com.xianj.community.controller;

import com.xianj.community.entity.DiscussPost;
import com.xianj.community.entity.Page;
import com.xianj.community.entity.User;
import com.xianj.community.service.DiscussPostService;
import com.xianj.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(path="/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        // DispatchServlet会自动初始化page对象，并将网页传入的参数set进page，然后将page注入model中，model也是DispatchServlet自动初始化的
        // 所以在thymeleaf中可以直接访问page对象
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");


        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());// 此时对象中的userid并非用户名，应查user进行组装
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list != null){
            for(DiscussPost post : list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

}