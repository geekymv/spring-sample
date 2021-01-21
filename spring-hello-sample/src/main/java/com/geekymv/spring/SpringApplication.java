package com.geekymv.spring;

import com.geekymv.spring.domain.User;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class SpringApplication {

    public static void main(String[] args) {

        Resource resource = new ClassPathResource("applicationContext.xml");

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);

        reader.loadBeanDefinitions(resource);


        User user = (User) beanFactory.getBean("user");
        System.out.println(user.getId() + ", " + user.getName());
    }

}
