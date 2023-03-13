package com.xianj.community;

import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// web开发  developer.mozilla.org/zh-CN
// 开发过程中，由controller处理浏览器请求，处理请求过程中，会调用业务组件（service）处理业务，业务组件调用dao访问数据库
// 过程即，controller调用service，service调用dao
@SpringBootApplication
public class CommunityApplication {
	@PostConstruct// 管理bean的生命周期
	public void init(){// 在构造函数之后执行
		// 解决netty启动冲突的问题（elasticsearch和mybatis都会启动netty，但elasticsearch会检查当前是否有netty启动，
		// 若有则抛出异常，所以我们需要修改使其不报异常，而正常执行）
		// see  Netty4Utils.setAvailableProcessors()
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}


