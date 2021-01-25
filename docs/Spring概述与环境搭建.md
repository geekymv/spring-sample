### Spring 概述

Spring 是于2003年兴起的一个轻量级的Java开发框架，由Rod Johnson 在其著作《Expert One-On-One J2EE Development and Design》中阐述的部分理念和原型衍生而来。Spring 是为了简化 Java EE 的企业级应用开发的复杂性而创建的。目前，Spring 已经成为事实上的 Java EE 开发标准，学习研究 Spring 框架已经成为每一位 Java开发人员的必修课。

Spring 框架提倡基于POJO(Plain Old Java Object，简单Java对象)的轻量级开发理念。

Spring 官网 https://spring.io，目前Spring 最新版本为5.3.3 文档

https://docs.spring.io/spring-framework/docs/current/reference/html/index.html

### Spring 核心组件介绍

#### Resource

Spring 框架内部使用 Resource 接口作为所有资源的抽象和访问接口，ClassPathResource 就是 Resource 接口的一个特定类型的实现，代表的是位于 classpath 中的资源。Resource 接口可以根据资源的不同类型给出相应的具体实现，Spring 中提供的 Resource 接口的更多实现待后面文章介绍。

### BeanDefinitionReader

Spring 中大部分功能都是通过配置完成的，配置文件的读取是Spring 中重要的功能。其中 XML 格式的配置是Spring 支持最完整，功能最强大的表达方式，XmlBeanDefinitionReader 就是Spring 提供的读取 XML 格式的配置文件的实现类。XmlBeanDefinitionReader 负责读取 Spring 指定格式的配置文件并解析，将解析后的 bean 映射到相应的 BeanDefinition 并加载到相应的 BeanFactory 中。

### BeanFactory

BeanFactory 是访问 Spring bean 容器的根接口，它提供了一些访问容器内管理的 bean 的方法。比如我们可以使用的 getBean(String name) 方法 返回 bean 实例。BeanFactory 只是一个接口，我们最终还是需要一个该接口的实现类来进行实际的 bean 管理，DefaultListableBeanFactory 就是比较通用的 BeanFactory 实现类。

DefaultListableBeanFactory 类间接实现了 BeanFactory 接口，还实现了 BeanDefinitionRegistry 接口。BeanDefinitionRegistry 接口定义了 bean 的注册逻辑，通常，具体的BeanFactory 实现类会是实现这个接口来管理 bean 的注册。

### 环境搭建

开发基于 Spring 的 Java 应用，一般步骤如下：

1、在 Spring 配置文件中声明 bean 的信息，创建一个名为 applicationContext.xml 的XML格式的配置文件。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="user" class="com.geekymv.spring.domain.User">
        <property name="id" value="1" />
        <property name="name" value="tom" />
    </bean>

</beans>
```

2、通过 Spring 抽象出的各种 Resource 来加载对应的配置文件；

3、声明一个 Spring bean 工厂(BeanFactory)，该工厂来管理我们在配置文件中定义的各种 bean 以及 bean 之间的依赖关系；

4、定义配置信息读取器 BeanDefinitionReader，用来读取之前所定义的 bean 配置文件，并将读取到的信息装配到声明的工厂当中；

5、将读取器与工厂以及资源对象进行相应的关联；

6、Bean 工厂所管理的全部对象装配完毕，客户端可以直接调用获取各种 bean 对象。

```java
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
```

完整代码 https://github.com/geekymv/spring-sample/tree/main/spring-hello-sample

通过上面几行代码，我们就可以实现一个简单的 Spring 应用了。而 Spring 框架在背后帮我们做了很多很多繁琐的工作，比如加载配置文件和解析注册 bean 等等。

至此，Spring 源码分析准备工作已经完成了，接下来会进行具体的 Spring 源码分析。



