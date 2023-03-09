package com.xianj.community.controller;

import com.xianj.community.annotation.LoginRequired;
import com.xianj.community.entity.Comment;
import com.xianj.community.entity.DiscussPost;
import com.xianj.community.service.CommentService;
import com.xianj.community.service.DiscussPostService;
import com.xianj.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    CommentService commentService;
    @Autowired
    HostHolder hostHolder;
    @RequestMapping(path = "/add/{postId}", method = RequestMethod.POST)
    @LoginRequired
    public String addComment(@PathVariable("postId") int postId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        return "redirect:/discuss/detail/" + postId;
    }
}
