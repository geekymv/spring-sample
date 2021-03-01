通过前面两篇文章的分析我们知道 Spring 将配置文件封装为 Resource。Resource 只负责资源文件的封装，而配置文件的读取工作则交给 BeanDefinitionReader 来完成。
Spring 中的大部分功能都是通过配置的方式实现的，其中以 XML 文件的形式最为常用。XML 配置文件的读取正是通过 XmlBeanDefinitionReader 类完成的，它的内部将 XML 文档的读取工作委托给 BeanDefinitionDocumentReader 接口的实现类DefaultBeanDefinitionDocumentReader 来完成。

### XmlBeanDefinitionReader
在我们的示例代码中，通过 XmlBeanDefinitionReader 构造方法创建，并接收 BeanDefinitionRegistry 对象，这里我们传入的是上篇文章分析的 BeanFactory 对象，因为 DefaultListableBeanFactory 对象实现了 BeanDefinitionRegistry 接口。
```java
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
```

### 继承关系
XmlBeanDefinitionReader 类的继承关系：

### 构造方法
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


```java
protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
    Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
    this.registry = registry;

    // Determine ResourceLoader to use.
    if (this.registry instanceof ResourceLoader) {
        this.resourceLoader = (ResourceLoader) this.registry;
    }
    else {
        this.resourceLoader = new PathMatchingResourcePatternResolver();
    }

    // Inherit Environment if possible
    if (this.registry instanceof EnvironmentCapable) {
        this.environment = ((EnvironmentCapable) this.registry).getEnvironment();
    }
    else {
        this.environment = new StandardEnvironment();
    }
}
```
AbstractBeanDefinitionReader 的构造方法中主要做了三件事：

1、根据传入的bean factory 创建 AbstractBeanDefinitionReader
将 registry 参数赋值给 AbstractBeanDefinitionReader 的成员变量 `private final BeanDefinitionRegistry registry;`。

2、判断传入的 bena factory 是否实现了 ResourceLoader 接口，如果 bean 工厂实现了 ResourceLoader 接口，则将它赋值给成员变量
`private ResourceLoader resourceLoader;`，否则将 resourceLoader 初始化为 PathMatchingResourcePatternResolver 对象。

3、判断传入的 bena factory 是否实现了 EnvironmentCapable 接口，如果 bean 工厂实现了 EnvironmentCapable 接口，则将它赋值给成员变量
  `private Environment environment;`，否则将 environment 初始化为 StandardEnvironment 对象。


注意，AbstractBeanDefinitionReader 是抽象类，无法直接调用构造方法创建实例，它的子类 XmlBeanDefinitionReader 在构造方法中通过 super() 调用调用父类的构造方法，这样抽象类的构造方法得到了执行。

平时很少使用抽象类的同学可以模仿 AbstractBeanDefinitionReader 的实现，
通过在子类的构造方法中调用 super() 方法来调用父类的构造方法完成初始化工作。

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
至此，已经完成了配置文件的封装、Bean工厂和类定义读取器的创建，接下来该加载和注册Bean的定义信息了，敬请期待。




