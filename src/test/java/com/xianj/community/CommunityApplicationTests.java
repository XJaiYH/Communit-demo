package com.xianj.community;

import com.xianj.community.config.AlphaConfig;
import com.xianj.community.dao.AlphaDao;
import com.xianj.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 使用main里面类的配置
class CommunityApplicationTests implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Test
	public void testApplicationContext() {
		System.out.println(applicationContext);
		AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);// 从容器中获取这个bean，是实例化的
		// 如果对于同一接口，有多个实现，此时如果仍按照接口类型去实现（装载）bean（实例化对象），那么spring就不知道应该实例化哪一个bean
		// 如此例子中，AlphaDao有两个实现类，可以通过@Primary注解设置哪一个bean被优先装配，spring就会优先装配该bean并返回
		System.out.println(alphaDao.select());

		alphaDao = (AlphaDao) applicationContext.getBean("alphaHibernate");
		System.out.println(alphaDao.select());
	}

	@Test
	public void testBeanManagement(){
		AlphaService alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);

		alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);
	}

	@Test
	public void testBeanConfig(){ // 如何在容器中装配第三方类？由于这些类是写在jar包里，我们无法去jar包中添加注解，所以自己写一个配置类
								  // 并在配置类中通过@Bean注解说明返回的实例要装载在容器中
		SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
		System.out.println(simpleDateFormat.format(new Date()));
	}

	@Autowired //声明容器将AlphaDao注入该属性（字段），如此我们就不需要在每次使用时调用application的getBean方法。（依赖注入）
	@Qualifier("alphaHibernate")
	private AlphaDao alphaDao; // 如果希望注入的不是优先级高的bean，如何操作？使用Qualifier注解+bean的名字
	@Autowired
	private AlphaService alphaService;
	@Autowired
	private SimpleDateFormat simpleDateFormat;

	@Test
	public void testDI(){
		System.out.println(alphaDao);
		System.out.println(alphaService);
		System.out.println(simpleDateFormat);
	}

}
