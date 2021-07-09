面试中经常会被问到Spring中Bean的生命周期问题，一般我们可能会在面试之前背背面试题然后给面试官说说。
那么Spring中如何定义Spring Bean的生命周期的呢？其实BeanFactory接口注释中已经明确说明了Spring Bean的生命周期。

Bean factory implementations should support the standard bean lifecycle interfaces as far as possible. The full set of initialization methods and their standard order is:
1.BeanNameAware's setBeanName
2.BeanClassLoaderAware's setBeanClassLoader
3.BeanFactoryAware's setBeanFactory
4.EnvironmentAware's setEnvironment
5.EmbeddedValueResolverAware's setEmbeddedValueResolver
6.ResourceLoaderAware's setResourceLoader (only applicable when running in an application context)
7.ApplicationEventPublisherAware's setApplicationEventPublisher (only applicable when running in an application context)
8.MessageSourceAware's setMessageSource (only applicable when running in an application context)
9.ApplicationContextAware's setApplicationContext (only applicable when running in an application context)
10.ServletContextAware's setServletContext (only applicable when running in a web application context)
上面10个都是XxxAware接口

11.postProcessBeforeInitialization methods of BeanPostProcessors
12.InitializingBean's afterPropertiesSet
13.a custom init-method definition
14.postProcessAfterInitialization methods of BeanPostProcessors


On shutdown of a bean factory, the following lifecycle methods apply:
1.postProcessBeforeDestruction methods of DestructionAwareBeanPostProcessors
2.DisposableBean's destroy
3.a custom destroy-method definition

下面我们回到源码层面看看Spring Bean生命周期实现过程

AbstractAutowireCapableBeanFactory 类
```java
/**
 * Actually create the specified bean. Pre-creation processing has already happened
 * at this point, e.g. checking {@code postProcessBeforeInstantiation} callbacks.
 * <p>Differentiates between default bean instantiation, use of a
 * factory method, and autowiring a constructor.
 * @param beanName the name of the bean
 * @param mbd the merged bean definition for the bean
 * @param args explicit arguments to use for constructor or factory method invocation
 * @return a new instance of the bean
 * @throws BeanCreationException if the bean could not be created
 * @see #instantiateBean
 * @see #instantiateUsingFactoryMethod
 * @see #autowireConstructor
 */
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
        throws BeanCreationException {

    // Instantiate the bean.
    BeanWrapper instanceWrapper = null;
    if (mbd.isSingleton()) {
        instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
    }
    if (instanceWrapper == null) {
        instanceWrapper = createBeanInstance(beanName, mbd, args);
    }
    final Object bean = instanceWrapper.getWrappedInstance();
    Class<?> beanType = instanceWrapper.getWrappedClass();
    if (beanType != NullBean.class) {
        mbd.resolvedTargetType = beanType;
    }

    // Allow post-processors to modify the merged bean definition.
    synchronized (mbd.postProcessingLock) {
        if (!mbd.postProcessed) {
            try {
                applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
            }
            catch (Throwable ex) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "Post-processing of merged bean definition failed", ex);
            }
            mbd.postProcessed = true;
        }
    }

    // Eagerly cache singletons to be able to resolve circular references
    // even when triggered by lifecycle interfaces like BeanFactoryAware.
    boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
            isSingletonCurrentlyInCreation(beanName));
    if (earlySingletonExposure) {
        if (logger.isTraceEnabled()) {
            logger.trace("Eagerly caching bean '" + beanName +
                    "' to allow for resolving potential circular references");
        }
        addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
    }

    // Initialize the bean instance.
    Object exposedObject = bean;
    try {
        // 属性填充
        populateBean(beanName, mbd, instanceWrapper);
        // 初始化bean
        exposedObject = initializeBean(beanName, exposedObject, mbd);
    }
    catch (Throwable ex) {
        if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
            throw (BeanCreationException) ex;
        }
        else {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
        }
    }

    // 省略部分代码...

    // Register bean as disposable.
    try {
        // 注册 DisposableBean 销毁方法
        registerDisposableBeanIfNecessary(beanName, bean, mbd);
    }
    catch (BeanDefinitionValidationException ex) {
        throw new BeanCreationException(
                mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
    }

    return exposedObject;
}
```