package com.xianj.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    // 某一个用户的赞
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    // 关注列表
    private static final String PREFIX_FOLLOWEE = "followee";
    // 粉丝列表
    private static final String PREFIX_FOLLOWER = "follower";
    // 验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // ticket
    private static final String PREFIX_TICKET = "ticket";
    // user
    private static final String PREFIX_USER = "user";

    // 某个实体的赞的key
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // like:user:userid -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 关注列表，关注的是人还是帖子？
    // followee:userId:entityType -> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体的粉丝列表
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }
}
