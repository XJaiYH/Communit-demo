package com.xianj.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.function.Consumer;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 使用main里面类的配置
public class KafkaTest {
    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka(){
        // 生产者发消息是我们主动调用发送，
        // 消费者取消息是被动调用，即有消息就获取
        kafkaProducer.sendMsg("test", "你好");
        kafkaProducer.sendMsg("test", "在吗");
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

@Component
class KafkaProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;
    // 生产者发消息是我们主动调用发送
    public void sendMsg(String topic, String content){
        kafkaTemplate.send(topic, content);
    }

}

@Component
class KafkaConsumer {
    @KafkaListener(topics = {"test"})
    // 消费者取消息是被动调用，即有消息就获取
    public void handleMsg(ConsumerRecord record){
        System.out.println(record.value());
    }
}