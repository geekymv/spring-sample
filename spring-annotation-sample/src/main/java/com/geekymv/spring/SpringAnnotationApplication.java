package com.geekymv.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringAnnotationApplication {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

        MyBean bean = ctx.getBean(MyBean.class);
        System.out.println(bean.getName());
    }
}
