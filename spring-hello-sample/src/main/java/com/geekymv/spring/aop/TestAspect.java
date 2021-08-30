package com.geekymv.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class TestAspect {


    @Pointcut("execution(* *.test(..))")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void before() {
        System.out.println("before");
    }

    @After("pointCut()")
    public void after() {
        System.out.println("after");
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint p) throws Throwable {
        System.out.println("around before");
        Object result = p.proceed();
        System.out.println("around after");

        return result;
    }
}
