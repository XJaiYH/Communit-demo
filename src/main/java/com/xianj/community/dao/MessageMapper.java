package com.xianj.community.dao;

import com.xianj.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    int insertMessage(Message message);
    // 修改消息状态
    int updateStatus(int id, int status);
    // 返回会话列表，每一个会话只返回一条最新私信
    List<Message> selectConversations(int userId, int offset, int limit);
    // 查询当前用户会话数量
    int selectConversationCount(int userId);
    // 查询某一个会话的所有消息列表
    List<Message> selectMessagesForSingleConv(String conversationId, int offset, int limit);
    // 查询某一个会话的所有消息的数量
    int selectMessageCountForSingleConv(String conversationId);
    // 查询未读私信数量，该方法可以查询该用户所有未读私信数量，也可查询某个会话的未读私信数量，通过sql拼接即可
    int selectMessageUnreadCount(int userId, String conversationId);

    // 查询某个主题最新的通知
    Message selectLatentNotice(int userId, String topic);
    // 查询某个主题包含的通知数
    int selectNoticeCount(int userId, String topic);
    // 查询未读通知数
    int selectNoticeUnreadCount(int userId, String topic);

    List<Message> selectNotices(int userId, String topic, int offset, int limit);

    List<Integer> selectUnreadNoticeIds(int userId, String topic);
}
