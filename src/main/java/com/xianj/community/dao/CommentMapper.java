package com.xianj.community.dao;

import com.xianj.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCommentRowsByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int commentId);
}
