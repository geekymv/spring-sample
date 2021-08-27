package com.geekymv.spring.event;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * ApplicationListener
 * 观察者模式
 *
 * AbstractApplicationContext.refresh()方法
 * // Initialize event multicaster for this context.
 * initApplicationEventMulticaster();
 * 初始化事件广播器
 *
 * // Check for listener beans and register them.
 * registerListeners();
 * 注册监听器
 *
 */
public class TestApplicationListener {

    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("event.xml");

        try {
            Method method = getDeclaredMethod(context, "getApplicationEventMulticaster");
            method.setAccessible(true);
            ApplicationEventMulticaster multicaster = (ApplicationEventMulticaster)method.invoke(context);
            System.out.println(multicaster.getClass());

        } catch (Exception e) {
            e.printStackTrace();
        }


        context.publishEvent(new TestEvent("event", "msg"));

        // BeanFactory 是ApplicationContext的一个属性（AbstractRefreshableApplicationContext）
       /* ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        beanFactory.registerSingleton("hello", new TestApplicationListener());


        Object hello = context.getBean("hello");
        System.out.println(hello.getClass());*/

    }

    public static Method getDeclaredMethod(Object object, String methodName, Class<?> ... parameterTypes){
        Method method = null;
        for(Class<?> clazz = object.getClass() ; clazz != Object.class ; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes) ;
                return method ;
            } catch (NoSuchMethodException e) {
            }
        }

        return method;
    }

}
