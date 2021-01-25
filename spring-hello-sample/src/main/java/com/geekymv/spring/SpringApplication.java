package com.geekymv.spring;

import com.geekymv.spring.domain.User;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class SpringApplication {

    public static void main(String[] args) {

        // 加载 bean 配置文件
        Resource resource = new ClassPathResource("applicationContext.xml");
        // 声明 Spring bean 工厂
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 定义 BeanDefinitionReader，并指定 BeanFactory
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);

        // 执行配置文件的解析，bean 信息装配等
        reader.loadBeanDefinitions(resource);

        // 获取 BeanFactory 管理的 bean 实例
        User user = (User) beanFactory.getBean("user");
        System.out.println(user.getId() + ", " + user.getName());
    }

}
