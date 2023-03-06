package com.xianj.community.util;

import com.xianj.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象
 * 其实就是对线程的私有数据进行隔离，set方法是存数据，get方法是读数据，clear是清除
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();
    public void setUser(User user){
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }
    public void clear(){// 用完就清理，不要占内存
        users.remove();
    }
}
