package com.xianj.community.dao;

import com.xianj.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {
    @Insert({"insert into login_ticket (user_id, ticket, status, expired) " +
            " values(#{userId}, #{ticket}, #{status}, #{expired})"})
    @Options(useGeneratedKeys = true, keyProperty = "id")// use...表示使用自动生成主键，keyPro...表示注入到哪个字段
    int insertLoginTicket(LoginTicket loginTicket);
    @Select({"select id, user_id, ticket, status, expired " +
            "from login_ticket where ticket=#{ticket}"})
    LoginTicket selectByTicket(String ticket);
    @Update({"<script>" +
            "update login_ticket set status=#{status} where ticket=#{ticket}" +
            "<if test=\"ticket!=null\">" +
            "and 1=1" + // 此处只是演示这样如何使用if写动态sql
            "</if>" +
            "</script>"})// 如何写动态sql？？？
    int updateStatus(String ticket, int status);
}
