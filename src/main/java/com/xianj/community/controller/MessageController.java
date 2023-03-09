package com.xianj.community.controller;

import com.xianj.community.entity.Message;
import com.xianj.community.entity.Page;
import com.xianj.community.entity.User;
import com.xianj.community.service.MessageService;
import com.xianj.community.service.UserService;
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
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    // 获取私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getMessageList(Model model, Page page){
        // 设置分页信息
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        // 查询会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(Message conversation : conversationList){
                Map<String, Object> conversationVo = new HashMap<>();
                conversationVo.put("conversation", conversation);
                conversationVo.put("messageCount", messageService.findMessageCountForSingleConv(conversation.getConversationId()));
                conversationVo.put("unreadCount", messageService.findMessageUnreadCount(user.getId(), conversation.getConversationId()));
                int targetId = conversation.getFromId() == user.getId() ? conversation.getToId() : conversation.getFromId();
                User u = userService.findUserById(targetId);
                conversationVo.put("target", u);
                conversations.add(conversationVo);
            }
        }
        model.addAttribute("conversations", conversations);
        model.addAttribute("messageCount", messageService.findMessageUnreadCount(user.getId(), null));
        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getConversationDetail(Model model, @PathVariable("conversationId") String conversationId, Page page){
        page.setPath("/letter/detail/" + conversationId);
        page.setLimit(5);
        page.setRows(messageService.findMessageCountForSingleConv(conversationId));
        List<Message> letterList = messageService.findMessagesForSingleConv(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        User user = hostHolder.getUser();
        if(letters != null){
            for(Message letter : letterList){
                if(letter.getStatus() == 0 && letter.getToId() == user.getId()){
                    messageService.updateStatus(letter.getId(), 1);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                map.put("user", userService.findUserById(letter.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        String[] strs = conversationId.split("_");
        int targetId = Integer.parseInt(strs[0]) == user.getId() ? Integer.parseInt(strs[1]) : Integer.parseInt(strs[0]);
        User target = userService.findUserById(targetId);
        model.addAttribute("target", target);
        return "/site/letter-detail";
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String username, String content){
        User user = hostHolder.getUser();
        if(user == null) {
            return CommunityUtil.getJSONString(403, "您还未登录！");
        }
        User target = userService.findUserByName(username);
        if(target == null) {
            return CommunityUtil.getJSONString(403, "发送对象不存在！");
        }
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        message.setFromId(user.getId());
        message.setToId(target.getId());
        String conversationId = user.getId() < target.getId() ? String.valueOf(user.getId()) + "_" + String.valueOf(target.getId()) : String.valueOf(target.getId()) + "_" + String.valueOf(user.getId());
        message.setConversationId(conversationId);
        messageService.sendMessage(message);
        return CommunityUtil.getJSONString(0, "发送成功！");
    }
}
