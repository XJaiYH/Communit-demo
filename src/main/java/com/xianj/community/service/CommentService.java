package com.xianj.community.service;

import com.xianj.community.dao.CommentMapper;
import com.xianj.community.entity.Comment;
import com.xianj.community.entity.DiscussPost;
import com.xianj.community.util.CommunityConstent;
import com.xianj.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstent {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private SensitiveFilter filter;
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentRows(int entityType, int entityId){
        return commentMapper.selectCommentRowsByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(filter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        // 更新帖子评论数量
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), post.getCommentCount()+1);
        }
        return rows;
    }

    public Comment findCommentById(int commentId){
        return commentMapper.selectCommentById(commentId);
    }
}
