AopNamespaceHandler

```java
registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser());
```

AspectJAutoProxyBeanDefinitionParser 从parse方法开始执行
```java
@Override
public BeanDefinition parse(Element element, ParserContext parserContext) {
    AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);
    extendBeanDefinition(element, parserContext);
    return null;
}
```
registerAspectJAnnotationAutoProxyCreatorIfNecessary 解析<aspectj-autoproxy /> 标签，
并将 AnnotationAwareAspectJAutoProxyCreator 注册到 BeanDefinitionRegistry
```java
public static void registerAspectJAnnotationAutoProxyCreatorIfNecessary(
			ParserContext parserContext, Element sourceElement) {
    // 将 AnnotationAwareAspectJAutoProxyCreator 的BeanDefinition对象 注册到 BeanDefinitionRegistry 中
    BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(
            parserContext.getRegistry(), parserContext.extractSource(sourceElement));
    
    // 解析 proxy-target-class 和 expose-proxy 属性
    useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
    registerComponentIfNecessary(beanDefinition, parserContext);
}
```
AnnotationAwareAspectJAutoProxyCreator 继承自 InstantiationAwareBeanPostProcessor 集成自 BeanPostProcessor


AbstractAutoProxyCreator 实现了

postProcessBeforeInstantiation 、postProcessAfterInitialization 方法





ThreadLocal使用
```java
/**
 * Search the given candidate Advisors to find all Advisors that
 * can apply to the specified bean.
 * @param candidateAdvisors the candidate Advisors
 * @param beanClass the target's bean class
 * @param beanName the target's bean name
 * @return the List of applicable Advisors
 * @see ProxyCreationContext#getCurrentProxiedBeanName()
 */
protected List<Advisor> findAdvisorsThatCanApply(
        List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {

    ProxyCreationContext.setCurrentProxiedBeanName(beanName);
    try {
        return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
    }
    finally {
        ProxyCreationContext.setCurrentProxiedBeanName(null);
    }
}
```

```java
@Override
protected List<Advisor> findCandidateAdvisors() {
    // Add all the Spring advisors found according to superclass rules.
    List<Advisor> advisors = super.findCandidateAdvisors();
    // Build Advisors for all AspectJ aspects in the bean factory.
    advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
    return advisors;
}
```

判断是否有Aspect注解
```java
public List<Advisor> buildAspectJAdvisors() {

    if (this.advisorFactory.isAspect(beanType)) {
    }
}
```


BeanPostProcessor 与 BeanFactoryPostProcessor
