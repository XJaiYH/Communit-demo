package com.xianj.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    // 随机字符串 激活码
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // MD5加密，只能加密不可解密，但对同一明文，其加密后的密文是一样的
    // 所以加密时，首先在明文密码后面添加一个随机字符串，再使用MD5加密
    public static String md5(String key){// 对密码的判读？
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
