package com.xianj.community.controller;

import com.xianj.community.annotation.LoginRequired;
import com.xianj.community.entity.Comment;
import com.xianj.community.entity.DiscussPost;
import com.xianj.community.entity.Event;
import com.xianj.community.event.EventProducer;
import com.xianj.community.service.CommentService;
import com.xianj.community.service.DiscussPostService;
import com.xianj.community.util.CommunityConstent;
import com.xianj.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstent {
    @Autowired
    CommentService commentService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    private EventProducer producer;
    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path = "/add/{postId}", method = RequestMethod.POST)
    @LoginRequired
    public String addComment(@PathVariable("postId") int postId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 构建事件对象, 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", postId);

        // entityUserId字段，要分是帖子还是评论
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(post.getUserId());
        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        producer.fireEvent(event);
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityId(postId)
                    .setEntityType(ENTITY_TYPE_POST);
            producer.fireEvent(event);
        }

        return "redirect:/discuss/detail/" + postId;

    }
}
