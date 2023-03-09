package com.xianj.community.service;

import com.xianj.community.dao.MessageMapper;
import com.xianj.community.entity.Message;
import com.xianj.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
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
        return messageMapper.insertMessage(message);
    }

    public int updateStatus(int id, int status){
        return messageMapper.updateStatus(id, status);
    }
}
