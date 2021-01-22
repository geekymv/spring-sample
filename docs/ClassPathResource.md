```java
Resource resource = new ClassPathResource("applicationContext.xml");
```

调用 ClassPathResource 类的构造方法，ClassPathResource  内部帮我们做了很多繁琐的工作。首先看下 ClassPathResource  类的继承关系：

![ClassPathResource](ClassPathResource.assets/ClassPathResource.png)

```java
public class ClassPathResource extends AbstractFileResolvingResource {

   private final String path;

   @Nullable
   private ClassLoader classLoader;

   @Nullable
   private Class<?> clazz;
   
   ...
}   
```

ClassPathResource 类内部 定义了 final 类型的变量path，用于存储构造方法传入的path参数。



```java
public ClassPathResource(String path) {
   this(path, (ClassLoader) null);
}
```

第一次看到 this(path, (ClassLoader) null); 这行代码的时候，有点懵的。一般我们使用 this()方法调用重载的构造方法时，参数少的调用参数多的构造方法，其他参数传默认值就行了，这里为什么要将null 强制转换成ClassLoader类型呢？

继续跟踪会发现 ClassPathResource 类提供了两个参数类型不同的构造方法 ClassPathResource(String, ClassLoader) 和 ClassPathResource(String, Class)，第二个参数类型不同。这种方式在我们的日常开发中可以尝试去使用。

```java
public ClassPathResource(String path, @Nullable ClassLoader classLoader) {
   Assert.notNull(path, "Path must not be null");
   String pathToUse = StringUtils.cleanPath(path);
   if (pathToUse.startsWith("/")) {
      pathToUse = pathToUse.substring(1);
   }
   this.path = pathToUse;
   this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
}
```



```java
public ClassPathResource(String path, @Nullable Class<?> clazz) {
   Assert.notNull(path, "Path must not be null");
   this.path = StringUtils.cleanPath(path);
   this.clazz = clazz;
}
```

对比上面两个构造方法的实现，对path 路径



Class类中的 getResourceAsStream() 方法 与 ClassLoader 类中的 getResourceAsStream() 关系？





ClassPathResource 还提供了三个参数的构造方法，不过已经不推荐使用了。

```java
@Deprecated
protected ClassPathResource(String path, @Nullable ClassLoader classLoader, @Nullable Class<?> clazz) {
   this.path = StringUtils.cleanPath(path);
   this.classLoader = classLoader;
   this.clazz = clazz;
}
```

> Deprecated as of 4.3.13, in favor of selective use of ClassPathResource(String, ClassLoader) vs ClassPathResource(String, Class)



ClassUtils.getDefaultClassLoader() 使用 Spring 提供的工具类 ClassUtils 获取默认的类加载器。

```java
public static ClassLoader getDefaultClassLoader() {
   ClassLoader cl = null;
   try {
      cl = Thread.currentThread().getContextClassLoader();
   }
   catch (Throwable ex) {
      // Cannot access thread context ClassLoader - falling back...
   }
   if (cl == null) {
      // No thread context class loader -> use class loader of this class.
      cl = ClassUtils.class.getClassLoader();
      if (cl == null) {
         // getClassLoader() returning null indicates the bootstrap ClassLoader
         try {
            cl = ClassLoader.getSystemClassLoader();
         }
         catch (Throwable ex) {
            // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
         }
      }
   }
   return cl;
}
```

首先获取线程上线文类加载器，如果获取失败，则获取加载 ClassUtils 类的类加载器，如果返回null（加载 ClassUtils 类的类加载器是启动类加载器），则获取系统类加载器。

至此，ClassPathResource 类的构造方法分析完毕。

































