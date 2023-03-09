package com.xianj.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {

    @Pointcut("execution(* com.xianj.community.service.*.*(..))")// 哪些bean的哪些方法是要处理的目标
    // service.*.*(..) 表示service里所有的类里面的所有的方法的所有的参数
    public void pointCut(){

    }

    @Before("pointCut()")// 在方法之前
    public void before(){
        System.out.println("in before");
    }

    @After("pointCut()")// 在方法之后
    public void after(){
        System.out.println("in after");
    }

    @AfterReturning("pointCut()")// 在返回值之后
    public void afterReturning(){
        System.out.println("in afterReturning");
    }

    @AfterThrowing("pointCut()")// 在抛异常之后
    public void afterThrowing(){
        System.out.println("in afterThrowing");
    }

    @Around("pointCut()")// 既在前面也在后面
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        System.out.println("around before");// 之前
        Object obj = joinPoint.proceed();// 调用目标组件
        System.out.println("around after");// 之后
        return obj;
    }

}
