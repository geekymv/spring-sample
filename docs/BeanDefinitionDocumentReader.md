Spring加载BeanDefinition过程详解（二）

BeanDefinitionDocumentReader 的实现类 DefaultBeanDefinitionDocumentReader

上一篇文章我们分析到 DefaultBeanDefinitionDocumentReader 注册 BeanDefinition 信息。
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
    // 获取root元素
    doRegisterBeanDefinitions(doc.getDocumentElement());
}
```

继续调用内部方法 doRegisterBeanDefinitions
```java
/**
 * Register each bean definition within the given root {@code <beans/>} element.
 */
@SuppressWarnings("deprecation")  // for Environment.acceptsProfiles(String...)
protected void doRegisterBeanDefinitions(Element root) {
    // Any nested <beans> elements will cause recursion in this method. In
    // order to propagate and preserve <beans> default-* attributes correctly,
    // keep track of the current (parent) delegate, which may be null. Create
    // the new (child) delegate with a reference to the parent for fallback purposes,
    // then ultimately reset this.delegate back to its original (parent) reference.
    // this behavior emulates a stack of delegates without actually necessitating one.
    BeanDefinitionParserDelegate parent = this.delegate;
    this.delegate = createDelegate(getReaderContext(), root, parent);

    if (this.delegate.isDefaultNamespace(root)) {
        // 处理profile属性
        String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
        if (StringUtils.hasText(profileSpec)) {
            String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
                    profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
            // We cannot use Profiles.of(...) since profile expressions are not supported
            // in XML config. See SPR-12458 for details.
            if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
                            "] not matching: " + getReaderContext().getResource());
                }
                return;
            }
        }
    }
    
    // 模板方法设计模式
    preProcessXml(root);
    // 解析BeanDefinition
    parseBeanDefinitions(root, this.delegate);
    postProcessXml(root);

    this.delegate = parent;
}
```
BeanDefinitionParserDelegate 类负责执行实际的 BeanDefinition 的解析工作。
首先判断beans节点是否定义了profile属性，本文测试配置applicationContext.xml没有设置profile属性，目前可以不用关注。

```java
preProcessXml(root);
parseBeanDefinitions(root, this.delegate);
postProcessXml(root);
```
考点，Spring中使用了哪些设计模式？
这里用到了模板方法设计模式，preProcessXml(root) 和 postProcessXml(root) 是空实现， DefaultBeanDefinitionDocumentReader 的子类可以重写这两个方法，可以在Bean解析前后做一些处理。


继续调用内部方法 parseBeanDefinitions，
```java
/**
 * Parse the elements at the root level in the document:
 * "import", "alias", "bean".
 * @param root the DOM root element of the document
 */
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
    if (delegate.isDefaultNamespace(root)) {
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if (delegate.isDefaultNamespace(ele)) {
                    // 解析XML默认命名空间(http://www.springframework.org/schema/beans)下的bean标签
                    parseDefaultElement(ele, delegate);
                }
                else {
                    delegate.parseCustomElement(ele);
                }
            }
        }
    }
    else {
        delegate.parseCustomElement(root);
    }
}
```

解析默认标签
```java
private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
    if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
        // 对import标签的处理
        importBeanDefinitionResource(ele);
    }
    else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
        // 对alias标签的处理
        processAliasRegistration(ele);
    }
    else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
        // 对bean标签的处理
        processBeanDefinition(ele, delegate);
    }
    else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
        // recurse
        doRegisterBeanDefinitions(ele);
    }
}
```

下面先看下对bean标签的处理过程
```java
/**
 * Process the given bean element, parsing the bean definition
 * and registering it with the registry.
 */
protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
    // 解析结果封装成BeanDefinitionHolder对象
    BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
    if (bdHolder != null) {
        // BeanDefinition 终于创建出来了
        bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
        try {
            // Register the final decorated instance.
            BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
        }
        catch (BeanDefinitionStoreException ex) {
            getReaderContext().error("Failed to register bean definition with name '" +
                    bdHolder.getBeanName() + "'", ele, ex);
        }
        // Send registration event.
        getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
    }
}
```

接下来，我们重点看下 BeanDefinitionHolder 对象的封装过程。

进入 BeanDefinitionParserDelegate  类的 parseBeanDefinitionElement 方法

```java
**
 * Parses the supplied {@code <bean>} element. May return {@code null}
 * if there were errors during parse. Errors are reported to the
 * {@link org.springframework.beans.factory.parsing.ProblemReporter}.
 */
public BeanDefinitionHolder parseBeanDefinitionElement(Element ele) {
    return parseBeanDefinitionElement(ele, null);
}
```


对照 applicationContext.xml 配置文件中的<bean>

```xml
<bean id="user" class="com.geekymv.spring.domain.User">
    <property name="id" value="1" />
    <property name="name" value="tom" />
</bean>
```

```java
/**
 * Parses the supplied {@code <bean>} element. May return {@code null}
 * if there were errors during parse. Errors are reported to the
 * {@link org.springframework.beans.factory.parsing.ProblemReporter}.
 */
@Nullable
public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, @Nullable BeanDefinition containingBean) {
    // 获取id属性
    String id = ele.getAttribute(ID_ATTRIBUTE);
    // 获取name属性，这里我们并没有配置name属性
    String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);

    List<String> aliases = new ArrayList<>();
    if (StringUtils.hasLength(nameAttr)) {
        // 如果配置了name属性，则将name属性按照,;拆分，添加到 aliases 列表中
        String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
        aliases.addAll(Arrays.asList(nameArr));
    }
    // 我们常说的beanName就是配置中的id属性
    String beanName = id;
    if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
        // 如果没有配置id属性，则使用name属性的第一个值
        beanName = aliases.remove(0); // 删除并返回
        if (logger.isTraceEnabled()) {
            logger.trace("No XML 'id' specified - using '" + beanName +
                    "' as bean name and " + aliases + " as aliases");
        }
    }

    if (containingBean == null) {
        // 校验id、name 是否重复
        checkNameUniqueness(beanName, aliases, ele);
    }
    
    // 封装成 AbstractBeanDefinition
    AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
    
    if (beanDefinition != null) {
        if (!StringUtils.hasText(beanName)) {
            // 处理beanName没有值的情况
            try {
                if (containingBean != null) {
                    beanName = BeanDefinitionReaderUtils.generateBeanName(
                            beanDefinition, this.readerContext.getRegistry(), true);
                }
                else {
                    beanName = this.readerContext.generateBeanName(beanDefinition);
                    // Register an alias for the plain bean class name, if still possible,
                    // if the generator returned the class name plus a suffix.
                    // This is expected for Spring 1.2/2.0 backwards compatibility.
                    String beanClassName = beanDefinition.getBeanClassName();
                    if (beanClassName != null &&
                            beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() &&
                            !this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
                        aliases.add(beanClassName);
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Neither XML 'id' nor 'name' specified - " +
                            "using generated bean name [" + beanName + "]");
                }
            }
            catch (Exception ex) {
                error(ex.getMessage(), ele);
                return null;
            }
        }
        // 将 GenericBeanDefinition 封装成 BeanDefinitionHolder
        String[] aliasesArray = StringUtils.toStringArray(aliases);
        return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
    }

    return null;
}
```

```java
/**
 * Validate that the specified bean name and aliases have not been used already
 * within the current level of beans element nesting.
 */
protected void checkNameUniqueness(String beanName, List<String> aliases, Element beanElement) {
    String foundName = null;

    if (StringUtils.hasText(beanName) && this.usedNames.contains(beanName)) {
        foundName = beanName;
    }
    if (foundName == null) {
        foundName = CollectionUtils.findFirstMatch(this.usedNames, aliases);
    }
    if (foundName != null) {
        error("Bean name '" + foundName + "' is already used in this <beans> element", beanElement);
    }

    this.usedNames.add(beanName);
    this.usedNames.addAll(aliases);
}
```


```java
/**
 * Parse the bean definition itself, without regard to name or aliases. May return
 * {@code null} if problems occurred during the parsing of the bean definition.
 */
public AbstractBeanDefinition parseBeanDefinitionElement(
        Element ele, String beanName, BeanDefinition containingBean) {

    this.parseState.push(new BeanEntry(beanName));

    String className = null;
    if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
        // 解析class属性值
        className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
    }

    try {
        String parent = null;
        if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
            parent = ele.getAttribute(PARENT_ATTRIBUTE);
        }
        // 创建 BeanDefinition
        AbstractBeanDefinition bd = createBeanDefinition(className, parent);
        
        // 解析<bean> 标签的其他属性（比如：scope、abstract、lazy-init、init-method 等）
        parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
        bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));

        parseMetaElements(ele, bd);
        parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
        parseReplacedMethodSubElements(ele, bd.getMethodOverrides());

        parseConstructorArgElements(ele, bd);
        parsePropertyElements(ele, bd);
        parseQualifierElements(ele, bd);

        bd.setResource(this.readerContext.getResource());
        bd.setSource(extractSource(ele));

        return bd;
    }
    catch (ClassNotFoundException ex) {
        error("Bean class [" + className + "] not found", ele, ex);
    }
    catch (NoClassDefFoundError err) {
        error("Class that bean class [" + className + "] depends on not found", ele, err);
    }
    catch (Throwable ex) {
        error("Unexpected failure during bean definition parsing", ele, ex);
    }
    finally {
        this.parseState.pop();
    }

    return null;
}
```

```java
/**
 * Create a bean definition for the given class name and parent name.
 * @param className the name of the bean class
 * @param parentName the name of the bean's parent bean
 * @return the newly created bean definition
 * @throws ClassNotFoundException if bean class resolution was attempted but failed
 */
protected AbstractBeanDefinition createBeanDefinition(String className, String parentName)
        throws ClassNotFoundException {

    return BeanDefinitionReaderUtils.createBeanDefinition(
            parentName, className, this.readerContext.getBeanClassLoader());
}
```
BeanDefinitionReaderUtils 工具类的 createBeanDefinition 方法

```java
/**
 * Create a new GenericBeanDefinition for the given parent name and class name,
 * eagerly loading the bean class if a ClassLoader has been specified.
 * @param parentName the name of the parent bean, if any
 * @param className the name of the bean class, if any
 * @param classLoader the ClassLoader to use for loading bean classes
 * (can be {@code null} to just register bean classes by name)
 * @return the bean definition
 * @throws ClassNotFoundException if the bean class could not be loaded
 */
public static AbstractBeanDefinition createBeanDefinition(
        String parentName, String className, ClassLoader classLoader) throws ClassNotFoundException {

    GenericBeanDefinition bd = new GenericBeanDefinition();
    bd.setParentName(parentName);
    if (className != null) {
        // 默认情况下 classLoader = null，AbstractBeanDefinitionReader 类中的 setBeanClassLoader 方法
        if (classLoader != null) {
            bd.setBeanClass(ClassUtils.forName(className, classLoader));
        }
        else {
            bd.setBeanClassName(className);
        }
    }
    return bd;
}
```
这里有个地方需要特别注意：
GenericBeanDefinition 类中的setBeanClass 和 setBeanClassName 都是将值赋值给 beanClass 成员变量，
说明 beanClass 成员变量既可以存储类的class对象，也可以存储类的全路径名，比如 com.geekymv.spring.domain.User。
```java

private volatile Object beanClass;

/**
 * Specify the bean class name of this bean definition.
 */
@Override
public void setBeanClassName(String beanClassName) {
    this.beanClass = beanClassName;
}

/**
 * Specify the bean class name of this bean definition.
 */
@Override
public void setBeanClassName(String beanClassName) {
    this.beanClass = beanClassName;
}
```









