package com.xianj.community.service;

import com.xianj.community.dao.AlphaDao;
import com.xianj.community.entity.listener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.swing.*;

@Service
@Scope("singleton") // 作用范围，整个容器中存在一个bean还是多个bean，默认为“singleton”，即单例。若为多个实例，则为prototype
                    // 多数情况下，只使用单例
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;
    @Autowired
    private listener ltr;

    public void printInfo(){
        Timer t = new Timer(1000, ltr);
        t.start();
    }

    public AlphaService(){
        System.out.println("实例化AlphaService");
    }

    @PostConstruct //初始化方法在构造器之后调用，用于初始化某些资源，自动调用
    public void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy //对象销毁前调用，可以在此释放某些资源，自动调用
    public void destroy(){
        System.out.println("销毁AlphaService");
    }

    public String find(){
        return alphaDao.select();
    }


}
