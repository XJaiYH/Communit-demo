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

    // 定义主题  评论 点赞 关注
    String TOPIC_COMMENT = "comment";
    String TOPIC_LIKE = "like";
    String TOPIC_FOLLOW = "follow";
    String TOPIC_PUBLISH = "publish";
    String TOPIC_DELETE = "delete";

    // 系统用户Id
    int SYSTEM_ID = 1;

    // 权限 用户
    String AUTHORITY_USER = "user";
    // 权限 管理员
    String AUTHORITY_ADMIN = "admin";
    // 权限 版主
    String AUTHORITY_MODERATOR = "moderator";

    // 帖子类型
    int POST_TOP = 1;
    int POST_WONDERFUL = 1;
    int POST_DELETE = 2;
}
