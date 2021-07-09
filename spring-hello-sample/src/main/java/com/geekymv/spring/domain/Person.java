package com.geekymv.spring.domain;

import com.geekymv.spring.UserAware;
import org.springframework.beans.factory.InitializingBean;

public class Person implements UserAware, InitializingBean {

    private User user;

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet");
    }

    private void myInitMethod() {
        System.out.println("myInitMethod");
    }
}
