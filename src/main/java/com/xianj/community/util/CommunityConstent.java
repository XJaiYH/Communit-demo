package com.xianj.community.util;

public interface CommunityConstent {

    // 激活成功
    int ACTIVATION_SUCCESS = 0;
    // 重复激活
    int ACTIVATION_REPEAT = 1;
    // 激活失败
    int ACTIVATION_FAILURE = 2;
    // 默认状态的登录凭证的超时时间
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
    // 记住状态的登录凭证的超时时间
    int REMEMBER_EXPIRED_SECONDS = 3600 * 12 * 100;

    // 实体类型为帖子
    int ENTITY_TYPE_POST = 1;

    // 实体类型为评论
    int ENTITY_TYPE_COMMENT = 2;

    // 用户类型为3
    int ENTITY_TYPE_USER = 3;

}
