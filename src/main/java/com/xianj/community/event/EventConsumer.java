package com.xianj.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.xianj.community.entity.DiscussPost;
import com.xianj.community.entity.Event;
import com.xianj.community.entity.Message;
import com.xianj.community.service.DiscussPostService;
import com.xianj.community.service.ElasticSearchService;
import com.xianj.community.service.MessageService;
import com.xianj.community.util.CommunityConstent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstent {

    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticSearchService elasticSearchService;
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    // 消费多种类型消息  私信、点赞、评论
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleMultiTypeMessage(ConsumerRecord record){
        if(record == null || record.value()==null){
            logger.error(()->"消息内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error(()->"消息格式错误，无法还原至事件类型！");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> map = new HashMap<>();
        map.put("userId", event.getUserId());
        map.put("entityType", event.getEntityType());
        map.put("entityId", event.getEntityId());
        map.put("topic", event.getTopic());
        if(!event.getData().isEmpty()){
            for(Map.Entry<String, Object> entry : event.getData().entrySet()){
                map.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(map));
        messageService.sendMessage(message);
    }

    // 消费发布的帖子事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if(record == null || record.value()==null){
            logger.error(()->"消息内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error(()->"消息格式错误，无法还原至事件类型！");
            return;
        }
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticSearchService.saveDiscussPost(post);
    }

    // 消费删除帖子事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if(record == null || record.value()==null){
            logger.error(()->"消息内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error(()->"消息格式错误，无法还原至事件类型！");
            return;
        }
        elasticSearchService.deleteDiscussPost(event.getEntityId());
    }
}
