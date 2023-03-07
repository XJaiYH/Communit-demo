package com.xianj.community.controller;

import com.xianj.community.entity.Comment;
import com.xianj.community.entity.DiscussPost;
import com.xianj.community.entity.Page;
import com.xianj.community.entity.User;
import com.xianj.community.service.CommentService;
import com.xianj.community.service.DiscussPostService;
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

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
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
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(Model model, @PathVariable("discussPostId") int discussPostId, Page page){
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

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
                        replayVoList.add(replayVo);
                    }
                }
                commentVo.put("replays", replayVoList);
                // 回复数量
                commentVo.put("replayCount", commentService.findCommentRows(ENTITY_TYPE_COMMENT, comment.getId()));
                // 封装每一个评论的commentVo
                commentVOList.add(commentVo);
            }
        }
        model.addAttribute("commentVOList", commentVOList);
        return "/site/discuss-detail";
    }
}
