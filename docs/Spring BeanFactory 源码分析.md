上篇文章我们分析了 ClassPathResource 资源文件的封装。
```java
Resource resource = new ClassPathResource("applicationContext.xml");
```
接下来，我们继续分析Spring中Bean工厂是如何创建的，我们通过调用 DefaultListableBeanFactory 类的构造方法创建一个 BeanFactory 对象。
```java
DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
```
```java
/**
 * Create a new DefaultListableBeanFactory.
 */
public DefaultListableBeanFactory() {
    super();
}
```
DefaultListableBeanFactory 类通过 super() 方法调用了父类 AbstractAutowireCapableBeanFactory 的无参构造方法，继续跟踪 super()方法
```java
/**
 * Create a new AbstractAutowireCapableBeanFactory.
 */
public AbstractAutowireCapableBeanFactory() {
    super();
    ignoreDependencyInterface(BeanNameAware.class);
    ignoreDependencyInterface(BeanFactoryAware.class);
    ignoreDependencyInterface(BeanClassLoaderAware.class);
}
```
同样的，AbstractAutowireCapableBeanFactory 首先通过 super() 方法调用了父类 AbstractBeanFactory 的无参构造方法，
AbstractBeanFactory 类的构造方法空实现。
```java
/**
 * Create a new AbstractBeanFactory.
 */
public AbstractBeanFactory() {
}
```
接着连续调用了3次 ignoreDependencyInterface 方法
```java
/**
 * Ignore the given dependency interface for autowiring.
 * <p>This will typically be used by application contexts to register
 * dependencies that are resolved in other ways, like BeanFactory through
 * BeanFactoryAware or ApplicationContext through ApplicationContextAware.
 * <p>By default, only the BeanFactoryAware interface is ignored.
 * For further types to ignore, invoke this method for each type.
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.context.ApplicationContextAware
 */
public void ignoreDependencyInterface(Class<?> ifc) {
    this.ignoredDependencyInterfaces.add(ifc);
}
```
ignoreDependencyInterface() 方法实现非常简单，就是将参数ifc 添加到成员变量 ignoredDependencyInterfaces 集合中。
```java
/**
 * Dependency interfaces to ignore on dependency check and autowire, as Set of
 * Class objects. By default, only the BeanFactory interface is ignored.
 */
private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<>();
```

调用了3次分别将 BeanNameAware、BeanFactoryAware、BeanClassLoaderAware 三个类的Class对象添加到 
成员变量 ignoredDependencyInterfaces 的 Set 集合中了，至于Set 集合中的元素后面会在什么地方使用，后面会分析。

看下 ignoreDependencyInterface() 方法的注释，翻译下就是
看起来有点懵，


