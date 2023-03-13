package com.xianj.community.controller;

import com.xianj.community.annotation.LoginRequired;
import com.xianj.community.entity.Event;
import com.xianj.community.entity.Page;
import com.xianj.community.entity.User;
import com.xianj.community.event.EventProducer;
import com.xianj.community.service.FollowService;
import com.xianj.community.service.UserService;
import com.xianj.community.util.CommunityConstent;
import com.xianj.community.util.CommunityUtil;
import com.xianj.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstent {

    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer producer;
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件 （系统向用户发送消息）
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId)// 因为当前只允许关注人，如果以后可以关注其他实体，此处需要修改
                .setUserId(user.getId());
        producer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "关注成功");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "取消关注");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){// 如果可以写一个错误的路径？
            throw new RuntimeException("用户不存在!");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> followeeList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        // 当前用户对集合里的用户的关注状态，因为集合里的用户可能是另一个用户的关注列表
        if(followeeList != null){
            for(Map<String, Object> map : followeeList){
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followeeList", followeeList);
        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){// 如果可以写一个错误的路径？
            throw new RuntimeException("用户不存在!");
        }
        // 当前用户所查看的某个用户的关注列表
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> followerList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        // 当前用户对集合里的用户的关注状态，因为集合里的用户可能是另一个用户的关注列表
        if(followerList != null){
            for(Map<String, Object> map : followerList){
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followerList", followerList);
        return "/site/follower";
    }

    public boolean hasFollowed(int entityId){
        if(hostHolder.getUser() != null){
            return followService.hasFollowed( hostHolder.getUser().getId(), ENTITY_TYPE_USER, entityId);
        }else{
            return false;
        }
    }
}
