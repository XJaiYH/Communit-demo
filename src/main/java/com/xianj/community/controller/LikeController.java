package com.xianj.community.controller;

import com.xianj.community.entity.Event;
import com.xianj.community.entity.User;
import com.xianj.community.event.EventProducer;
import com.xianj.community.service.LikeService;
import com.xianj.community.util.CommunityConstent;
import com.xianj.community.util.CommunityUtil;
import com.xianj.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstent {
    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;
    @Autowired
    private EventProducer producer;
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(entityType, entityId, user.getId(), entityUserId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        int likeStatus = likeService.findLikeStatus(entityType, entityId, user.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件（系统向用户发通知），点赞时通知就行！
        if(likeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setUserId(user.getId())
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            producer.fireEvent(event);
        }
        return CommunityUtil.getJSONString(0, null, map);
    }
}
