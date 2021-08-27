package com.geekymv.spring.event;

import org.springframework.context.ApplicationListener;

public class TestListener implements ApplicationListener<TestEvent> {

    @Override
    public void onApplicationEvent(TestEvent event) {
        System.out.println("thread name is  = " + Thread.currentThread().getName());

        event.print();
    }
}
