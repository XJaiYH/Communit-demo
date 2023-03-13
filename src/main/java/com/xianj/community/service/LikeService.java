package com.xianj.community.service;

import com.xianj.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeService {
    @Autowired
    RedisTemplate redisTemplate;

    // 点赞
    public void like(int entityType, int entityId, int userId, int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                operations.multi();// 先提交查询，才能获取isMember的值，才能继续后面的逻辑
                if(!isMember){
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }else{
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    // 查询实体的点赞量
    public long findEntityLikeCount(int entityType, int entityId){
        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(redisKey);
    }

    // 查询点赞状态
    public int findLikeStatus(int entityType, int entityId, int userId) {
        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        if(redisTemplate.opsForSet().isMember(redisKey, userId)){
            return 1;
        }else{
            return 0;
        }
    }

    // 查询某用户获得的赞
    public int getUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }

}
