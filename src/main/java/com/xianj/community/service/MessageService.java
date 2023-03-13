package com.xianj.community.service;

import com.xianj.community.dao.MessageMapper;
import com.xianj.community.entity.Message;
import com.xianj.community.util.RedisKeyUtil;
import com.xianj.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SensitiveFilter filter;

    @Autowired
    private RedisTemplate redisTemplate;
    public List<Message> findConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public List<Message> findMessagesForSingleConv(String conversationId, int offset, int limit){
        return messageMapper.selectMessagesForSingleConv(conversationId, offset, limit);
    }

    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    public int findMessageUnreadCount(int userId, String conversationId){
        return messageMapper.selectMessageUnreadCount(userId, conversationId);
    }

    public int findMessageCountForSingleConv(String conversation_id){
        return messageMapper.selectMessageCountForSingleConv(conversation_id);
    }

    public int sendMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(filter.filter(message.getContent()));
        int rows = messageMapper.insertMessage(message);
        return rows;
    }

    public int updateStatus(int id, int status){
        int rows = messageMapper.updateStatus(id, status);
        return rows;
    }

    public Message findLatentNotice(int userId, String topic){
        return messageMapper.selectLatentNotice(userId, topic);
    }

    public int findNoticeCount(int userId, String topic){
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findUnreadNoticeCount(int userId, String topic){
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    public List<Message> findNotices(int userId, String topic, int offset, int limit){
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }

    public List<Integer> findUnreadNoticeIds(int userId, String topic){
        return messageMapper.selectUnreadNoticeIds(userId, topic);
    }
}
