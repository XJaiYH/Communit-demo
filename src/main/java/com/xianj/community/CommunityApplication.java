package com.xianj.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.awt.*;

// web开发  developer.mozilla.org/zh-CN
// 开发过程中，由controller处理浏览器请求，处理请求过程中，会调用业务组件（service）处理业务，业务组件调用dao访问数据库
// 过程即，controller调用service，service调用dao
@SpringBootApplication
public class CommunityApplication {

	public static void main(String[] args) {
		//JOptionPane.showMessageDialog(null, "quit?"); //显示对话框，参数1为null表示在屏幕中间，参数2为提示信息
		SpringApplication.run(CommunityApplication.class, args);
	}

}


