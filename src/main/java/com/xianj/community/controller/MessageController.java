package com.xianj.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.xianj.community.entity.Message;
import com.xianj.community.entity.Page;
import com.xianj.community.entity.User;
import com.xianj.community.service.MessageService;
import com.xianj.community.service.UserService;
import com.xianj.community.util.CommunityConstent;
import com.xianj.community.util.CommunityUtil;
import com.xianj.community.util.HostHolder;
import org.apache.kafka.common.network.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstent {
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
        model.addAttribute("noticeUnreadCount", messageService.findUnreadNoticeCount(user.getId(), null));
        model.addAttribute("letterUnreadCount", messageService.findMessageUnreadCount(user.getId(), null));
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

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();

        Map<String, Object> messageVo = new HashMap<>();
        Message message = messageService.findLatentNotice(user.getId(), TOPIC_COMMENT);
        messageVo.put("message", message);
        if(message != null){

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((int) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));
            messageVo.put("count", messageService.findNoticeCount(user.getId(), TOPIC_COMMENT));
            messageVo.put("countUnread", messageService.findUnreadNoticeCount(user.getId(), TOPIC_COMMENT));
        }
        model.addAttribute("comment", messageVo);

        messageVo = new HashMap<>();
        message = messageService.findLatentNotice(user.getId(), TOPIC_LIKE);
        messageVo.put("message", message);
        if(message != null){

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((int) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));
            messageVo.put("count", messageService.findNoticeCount(user.getId(), TOPIC_LIKE));
            messageVo.put("countUnread", messageService.findUnreadNoticeCount(user.getId(), TOPIC_LIKE));
        }
        model.addAttribute("like", messageVo);

        messageVo = new HashMap<>();
        message = messageService.findLatentNotice(user.getId(), TOPIC_FOLLOW);
        messageVo.put("message", message);
        if(message != null){

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((int) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));// follow里没存postid，但取空值问题不大，前端判断下就好
            messageVo.put("count", messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW));
            messageVo.put("countUnread", messageService.findUnreadNoticeCount(user.getId(), TOPIC_FOLLOW));
        }
        model.addAttribute("follow", messageVo);
        model.addAttribute("noticeUnreadCount", messageService.findUnreadNoticeCount(user.getId(), null));
        model.addAttribute("letterUnreadCount", messageService.findMessageUnreadCount(user.getId(), null));

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNotices(Model model, @PathVariable("topic") String topic, Page page){
         User user = hostHolder.getUser();
         page.setRows(messageService.findNoticeCount(user.getId(), topic));
         page.setLimit(5);
         page.setPath("/notice/detail/" + topic);
         List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
         List<Map<String, Object>> noticeList = new ArrayList<>();
         if(notices != null){
             for(Message notice : notices){
                 Map<String, Object> map = new HashMap<>();
                 // 通知
                 map.put("notice", notice);
                 // 内容，因为内容是JSON字符串，应该转换后再传给页面
                 Map<String, Object> content = JSONObject.parseObject(HtmlUtils.htmlUnescape(notice.getContent()), HashMap.class);
                 map.put("user", userService.findUserById((Integer) content.get("userId")));
                 map.put("entityType", content.get("entityType"));
                 map.put("entityId", content.get("entityId"));
                 map.put("postId", content.get("postId"));
                 map.put("fromUser", userService.findUserById(notice.getFromId()));
                 noticeList.add(map);
                 // 设置已读，但更可行的方法是，在所有的消息查询完后，再查询未读的消息id，然后一起设置为已读，
                 // 这一可以避免在查询消息的时候发生错误，导致有的消息设置为已读，但并没有返回给页面，导致下次再次访问时，未读的消息丢失（变为已读）
             }
         }
         model.addAttribute("notices", noticeList);
         model.addAttribute("topic", topic);
         List<Integer> ids = messageService.findUnreadNoticeIds(user.getId(), topic);
         if(ids != null){
             for(Integer id : ids){
                 messageService.updateStatus((int) id, 1);
             }
         }
         return "/site/notice-detail";
    }
}
