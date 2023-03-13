package com.xianj.community.dao;

import com.xianj.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);// 用于动态sql查询，有时需要查询属于userId的(如个人主页)，有时不需要userId进行查询(如首页)

    // Param注解用于给参数起别名，
    // 如果该方法只有一个参数，并且在<if>里使用，则必须加别名(见DiscussPost-mapper.xml)。
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);
    int updateStatus(int id, int status);
}
