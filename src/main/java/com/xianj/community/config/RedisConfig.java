package com.xianj.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){// 参数的bean被容器装配
        // 要想访问数据库，应先建立连接，使用连接工厂
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 指定数据转换方式，即设置数据序列化方式
        // key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // value的序列化方式，通常序列化为json格式
        template.setValueSerializer(RedisSerializer.json());
        // 设置hash的key的序列化方式  因为hash中又有key-value
        template.setHashKeySerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet();
        return template;
    }

}
