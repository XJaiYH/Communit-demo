package com.xianj.community.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
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

    public static String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if(map != null){
            for(String key : map.keySet()){
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    // 如果没有提示消息和map呢？重载方法
    public static String getJSONString(int code, String msg){
        return getJSONString(code, msg, null);
    }
    public static String getJSONString(int code){
        return getJSONString(code, null, null);
    }
    public static void main(String[] args){
        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhang");
        map.put("age", 2);
        System.out.println(getJSONString(200, "msg", map));
    }
}
