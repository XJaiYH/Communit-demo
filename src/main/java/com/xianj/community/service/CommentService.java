package com.xianj.community.service;

import com.xianj.community.dao.CommentMapper;
import com.xianj.community.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentRows(int entityType, int entityId){
        return commentMapper.selectCommentRowsByEntity(entityType, entityId);
    }
}
