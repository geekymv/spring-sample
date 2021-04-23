加载BeanDefinition
https://www.cnblogs.com/VergiLyn/p/6130188.html
```java
/**
 * Load bean definitions from the specified XML file.
 * @param resource the resource descriptor for the XML file
 * @return the number of bean definitions found
 * @throws BeanDefinitionStoreException in case of loading or parsing errors
 */
@Override
public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
    return loadBeanDefinitions(new EncodedResource(resource));
}
```

首先将Resource封装成EncodedResource，同样的EncodedResource 也实现了InputStreamSource接口。

EncodedResource 指定编码的Resource，EncodedResource  内部维护了 Resource 的引用，将 Resource 和 编码封装在一起。

```java
public class EncodedResource implements InputStreamSource {

	private final Resource resource;

	@Nullable
	private final String encoding;

	@Nullable
	private final Charset charset;
    
    /**
	 * Create a new {@code EncodedResource} for the given {@code Resource},
	 * not specifying an explicit encoding or {@code Charset}.
	 * @param resource the {@code Resource} to hold (never {@code null})
	 */
	public EncodedResource(Resource resource) {
		this(resource, null, null);
	}
    
    private EncodedResource(Resource resource, @Nullable String encoding, @Nullable Charset charset) {
		super();
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
		this.encoding = encoding;
		this.charset = charset;
	}
    
    /**
	 * Open an {@code InputStream} for the specified resource, ignoring any specified
	 * {@link #getCharset() Charset} or {@linkplain #getEncoding() encoding}.
	 * @throws IOException if opening the InputStream failed
	 * @see #requiresReader()
	 * @see #getReader()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		return this.resource.getInputStream();
	}
    
    /**
	 * Return the encoding to use for reading from the {@linkplain #getResource() resource},
	 * or {@code null} if none specified.
	 */
	@Nullable
	public final String getEncoding() {
		return this.encoding;
	}
    
    // 其他方法，此处省略
}    
```



loadBeanDefinitions 内部调用了重载方法 loadBeanDefinitions(EncodedResource encodedResource)

```java
/**
	 * Load bean definitions from the specified XML file.
	 * @param encodedResource the resource descriptor for the XML file,
	 * allowing to specify an encoding to use for parsing the file
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 */
public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
    Assert.notNull(encodedResource, "EncodedResource must not be null");
    if (logger.isTraceEnabled()) {
        logger.trace("Loading XML bean definitions from " + encodedResource);
    }
	// 判断资源文件是否循环加载
    Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
    if (currentResources == null) {
        currentResources = new HashSet<>(4);
        this.resourcesCurrentlyBeingLoaded.set(currentResources);
    }
    if (!currentResources.add(encodedResource)) {
        throw new BeanDefinitionStoreException(
            "Detected cyclic loading of " + encodedResource + " - check your import definitions!");
    }
    try {
        // 从Resource 中获取输入流InputStream
        InputStream inputStream = encodedResource.getResource().getInputStream();
        try {
            // 将InputStream 封装成InputSource，并指定编码
            InputSource inputSource = new InputSource(inputStream);
            if (encodedResource.getEncoding() != null) {
                inputSource.setEncoding(encodedResource.getEncoding());
            }
            return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
        }
        finally {
            // 关闭输入流
            inputStream.close();
        }
    }
    catch (IOException ex) {
        throw new BeanDefinitionStoreException(
            "IOException parsing XML document from " + encodedResource.getResource(), ex);
    }
    finally {
        currentResources.remove(encodedResource);
        if (currentResources.isEmpty()) {
            this.resourcesCurrentlyBeingLoaded.remove();
        }
    }
}
```

loadBeanDefinitions 方法内部有两个值得关注的地方：

1、Spring是如何判断资源文件循环加载的（通过import标签导入自身）

resourcesCurrentlyBeingLoaded 成员变量是ThreadLocal的子类NamedThreadLocal，内部通过 HashSet 判断资源文件的循环加载。（考点，HashSet 底层是 HashMap实现的，EncodedResource 内部重写了equals 和 hashCode 方法）。使用ThreadLocal最后一定要remove，避免内存泄漏。

https://zhuanlan.zhihu.com/p/354153342

2、EncodedResource 封装的编码在何处使用的

















