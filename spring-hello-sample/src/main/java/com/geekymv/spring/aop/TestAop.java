package com.geekymv.spring.aop;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestAop {

    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("aop.xml");
        TestBean testBean = (TestBean) context.getBean("testBean");

        testBean.test();
    }

}
