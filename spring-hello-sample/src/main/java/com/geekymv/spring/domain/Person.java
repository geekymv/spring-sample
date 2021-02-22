package com.geekymv.spring.domain;

import com.geekymv.spring.UserAware;

public class Person implements UserAware {

    private User user;

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
