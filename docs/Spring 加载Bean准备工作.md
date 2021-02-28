通过前面两篇文章的分析我们知道 Spring 将配置文件封装为 Resource。
Resource 只负责资源文件的封装，而配置文件的读取工作则交给 BeanDefinitionReader 来完成。
Spring 中的大部分功能都是通过配置的方式实现的，其中以 XML 文件的形式最为常用。
XML 配置文件的读取正是通过 XmlBeanDefinitionReader 类完成的，它的内部将 XML 文档的读取工作委托
给 BeanDefinitionDocumentReader 接口的实现类 DefaultBeanDefinitionDocumentReader 来完成。

在我们的示例代码中，通过 XmlBeanDefinitionReader 构造方法创建，并接收 BeanDefinitionRegistry 对象，
这里我们传入的是上篇文章分析的 BeanFactory 对象，因为 DefaultListableBeanFactory 对象实现了 BeanDefinitionRegistry 接口。
```java
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
```

XmlBeanDefinitionReader 类的继承关系：



```java
/**
 * Create new XmlBeanDefinitionReader for the given bean factory.
 * @param registry the BeanFactory to load bean definitions into,
 * in the form of a BeanDefinitionRegistry
 */
public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
    super(registry);
}
```

XmlBeanDefinitionReader 构造方法中通过super() 将 registry 传递给父类 AbstractBeanDefinitionReader。





