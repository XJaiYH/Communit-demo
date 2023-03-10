package com.xianj.community.controller;

import com.xianj.community.annotation.LoginRequired;
import com.xianj.community.entity.*;
import com.xianj.community.event.EventProducer;
import com.xianj.community.service.CommentService;
import com.xianj.community.service.DiscussPostService;
import com.xianj.community.service.LikeService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstent {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403, "您还未登录");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        // 如果报错，将来统一处理
        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(discussPost.getId())
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(Model model, @PathVariable("discussPostId") int discussPostId, Page page){
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        int likeStatus = hostHolder.getUser() == null? 0 :
                likeService.findLikeStatus(ENTITY_TYPE_POST, post.getId(), hostHolder.getUser().getId());
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("likeStatus", likeStatus);

        page.setPath("/discuss/detail/" + discussPostId);
        page.setLimit(5);
        page.setRows(post.getCommentCount());
        // 对帖子的评论
        // 评论列表
        List<Comment> comments = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if(comments != null){
            for(Comment comment : comments) {
                // 一个评论的VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 评论的作者
                User u = userService.findUserById(comment.getUserId());
                commentVo.put("user", u);

                // 对评论的回复
                List<Comment> replays = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replayVoList = new ArrayList<>();
                if (replays != null) {
                    for (Comment replay : replays) {
                        Map<String, Object> replayVo = new HashMap<>();
                        // 回复
                        replayVo.put("replay", replay);
                        // 作者
                        replayVo.put("user", userService.findUserById(replay.getUserId()));
                        // 回复目标
                        User targetUser = replay.getTargetId() == 0 ? null : userService.findUserById(replay.getTargetId());
                        replayVo.put("target", targetUser);
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, replay.getId());
                        likeStatus = hostHolder.getUser() == null? 0 :
                                likeService.findLikeStatus(ENTITY_TYPE_COMMENT, post.getId(), hostHolder.getUser().getId());
                        replayVo.put("likeCount", likeCount);
                        replayVo.put("likeStatus", likeStatus);
                        replayVoList.add(replayVo);
                    }
                }
                commentVo.put("replays", replayVoList);
                // 回复数量
                commentVo.put("replayCount", commentService.findCommentRows(ENTITY_TYPE_COMMENT, comment.getId()));
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                likeStatus = hostHolder.getUser() == null? 0 :
                        likeService.findLikeStatus(ENTITY_TYPE_COMMENT, post.getId(), hostHolder.getUser().getId());
                commentVo.put("likeCount", likeCount);
                commentVo.put("likeStatus", likeStatus);
                // 封装每一个评论的commentVo
                commentVOList.add(commentVo);
            }
        }

        model.addAttribute("commentVOList", commentVOList);

        return "/site/discuss-detail";
    }

    // 置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int postId){
        discussPostService.updateType(postId, POST_TOP);
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(postId)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
    // 加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int postId){
        discussPostService.updateStatus(postId, POST_WONDERFUL);
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(postId)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deletePost(int postId){
        discussPostService.updateStatus(postId, POST_DELETE);
        // 触发删除帖子的事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(postId)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
}
