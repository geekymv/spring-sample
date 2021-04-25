Spring加载BeanDefinition过程详解（一）

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

考虑到Resource的读取可能存在编码要求，首先将Resource封装成EncodedResource，同样的EncodedResource 也实现了InputStreamSource接口。

EncodedResource 封装指定编码的Resource，EncodedResource  内部维护了 Resource 的引用，将 Resource 和编码封装在一起。

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
	 * Open a {@code java.io.Reader} for the specified resource, using the specified
	 * {@link #getCharset() Charset} or {@linkplain #getEncoding() encoding}
	 * (if any).
	 * @throws IOException if opening the Reader failed
	 * @see #requiresReader()
	 * @see #getInputStream()
	 */
	public Reader getReader() throws IOException {
		if (this.charset != null) {
			return new InputStreamReader(this.resource.getInputStream(), this.charset);
		}
		else if (this.encoding != null) {
			return new InputStreamReader(this.resource.getInputStream(), this.encoding);
		}
		else {
			return new InputStreamReader(this.resource.getInputStream());
		}
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
	// 判断资源文件是否循环加载.
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
        // 从Resource 中获取输入流InputStream.
        InputStream inputStream = encodedResource.getResource().getInputStream();
        try {
            // 将Resource中的InputStream封装成InputSource，并指定编码.
            InputSource inputSource = new InputSource(inputStream);
            if (encodedResource.getEncoding() != null) {
                inputSource.setEncoding(encodedResource.getEncoding());
            }
            // 调动内部方法
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

InputStream 类是JDK提供的位于org.xml.sax包下，Spring采用SAX解析XML配置文件。

loadBeanDefinitions 方法内部有个值得关注的地方，Spring是如何判断资源文件循环加载的（通过import标签导入自身）呢？

```java
Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
```

resourcesCurrentlyBeingLoaded 成员变量是ThreadLocal的子类NamedThreadLocal，内部通过 HashSet 判断资源文件的循环加载。（考点，HashSet 底层是 HashMap实现的，EncodedResource 内部重写了equals 和 hashCode 方法）。使用ThreadLocal最后一定要remove，避免内存泄漏（https://zhuanlan.zhihu.com/p/354153342）。

InputStream inputStream = encodedResource.getResource().getInputStream(); 这样代码解释了xxx最后留下的问题，ClassPathResource 类的 getInputStream() 方法在什么地方被执行的呢？



继续跟踪代码，实际执行从指定的XML文件加载BeanDefinition的方法。

```java
/**
 * Actually load bean definitions from the specified XML file.
 * @param inputSource the SAX InputSource to read from
 * @param resource the resource descriptor for the XML file
 * @return the number of bean definitions found
 * @throws BeanDefinitionStoreException in case of loading or parsing errors
 * @see #doLoadDocument
 * @see #registerBeanDefinitions
 */
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
    throws BeanDefinitionStoreException {

    try {
        // 将XML文件解析成Document对象
        Document doc = doLoadDocument(inputSource, resource);
        // 注册BeanDefinition
        int count = registerBeanDefinitions(doc, resource);
        if (logger.isDebugEnabled()) {
            logger.debug("Loaded " + count + " bean definitions from " + resource);
        }
        return count;
    }
    // 省略异常捕获部分代码
}

```



通过BeanDefinitionDocumentReader 注册BeanDefinition。

```java
/**
 * Register the bean definitions contained in the given DOM document.
 * Called by {@code loadBeanDefinitions}.
 * <p>Creates a new instance of the parser class and invokes
 * {@code registerBeanDefinitions} on it.
 * @param doc the DOM document
 * @param resource the resource descriptor (for context information)
 * @return the number of bean definitions found
 * @throws BeanDefinitionStoreException in case of parsing errors
 * @see #loadBeanDefinitions
 * @see #setDocumentReaderClass
 * @see BeanDefinitionDocumentReader#registerBeanDefinitions
 */
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
    // 通过反射的方式创建DefaultBeanDefinitionDocumentReader的实例
    BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
    // 获取之前容器中的BeanDefinition数量
    int countBefore = getRegistry().getBeanDefinitionCount();
    // BeanDefinitionDocumentReader 负责实际的Bean注册
    documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
    // 返回新增的BeanDefinition数量
    return getRegistry().getBeanDefinitionCount() - countBefore;
}


/**
 * Create the {@link BeanDefinitionDocumentReader} to use for actually
 * reading bean definitions from an XML document.
 * <p>The default implementation instantiates the specified "documentReaderClass".
 * @see #setDocumentReaderClass
 */
protected BeanDefinitionDocumentReader createBeanDefinitionDocumentReader() {
    return BeanUtils.instantiateClass(this.documentReaderClass);
}

```

```java
getRegistry().getBeanDefinitionCount();
```

BeanDefinitionRegistry 的实现类 DefaultListableBeanFactory 中 Map<String, BeanDefinition> beanDefinitionMap 中获取BeanDefinition的个数。

九转十八弯，流程最后转入BeanDefinitionDocumentReader 接口的实现类DefaultBeanDefinitionDocumentReader 中。

```java
/**
 * This implementation parses bean definitions according to the "spring-beans" XSD
 * (or DTD, historically).
 * <p>Opens a DOM Document; then initializes the default settings
 * specified at the {@code <beans/>} level; then parses the contained bean definitions.
 */
@Override
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
    this.readerContext = readerContext;
    doRegisterBeanDefinitions(doc.getDocumentElement());
}
```















