package cn.huangxin.iaa.core;

import cn.huangxin.iaa.annotation.Autowired;
import cn.huangxin.iaa.annotation.Component;
import cn.huangxin.iaa.annotation.ComponentScan;
import cn.huangxin.iaa.annotation.Scope;
import cn.huangxin.iaa.util.FileUtil;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 黄鑫
 * @description: 注解导入的上下文
 */
public class AnnotationConfigApplicationContext {

    private final Class<?> configClass;

    /**
     * bean定义容器
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 后置处理器集合
     */
    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    /**
     * 一级缓存（单例池，存放成品bean）
     */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * 二级缓存（存放半成品bean）
     */
    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

    /**
     * 三级缓存（存放刚实例化的bean）
     */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);


    public AnnotationConfigApplicationContext(Class<?> configClass) {
        this.configClass = configClass;

        // 扫描
        scanBeanDefinition(configClass);
        // 生成后置处理器
        registerBeanPostProcessors();
        // 实例化bean
        preInstantiateSingletons();
    }


    /**
     * 扫描被注解标记的bean -> 生成BeanDefinition放到beanDefinitionMap
     *
     * @param configClass
     */
    private void scanBeanDefinition(Class<?> configClass) {
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = configClass.getDeclaredAnnotation(ComponentScan.class);
            // cn.huangxin.test
            String basePackage = componentScan.value();
            // cn/huangxin/test
            String path = basePackage.replace(".", "/");
            // D:/code/IAA/target/classes/cn/huangxin/test
            String realPath = configClass.getClassLoader().getResource(path).getFile();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (String filePath : FileUtil.getAllFile(realPath)) {
                if (filePath.endsWith(".class")) {
                    try {
                        // filePath = D:\code\IAA\target\classes\cn\huangxin\test\AppConfig.class
                        String replace = filePath.replace(File.separator, ".");
                        String temp = replace.substring(replace.indexOf(basePackage));
                        // fullClassName = cn.huangxin.test.AppConfig
                        String fullClassName = temp.substring(0, temp.lastIndexOf("."));
                        Class<?> aClass = classLoader.loadClass(fullClassName);
                        if (aClass.isAnnotationPresent(Component.class)) {
                            Component component = aClass.getDeclaredAnnotation(Component.class);
                            String beanName = component.value();
                            // 默认beanName
                            if (beanName.trim().isEmpty()) {
                                beanName = Introspector.decapitalize(aClass.getSimpleName());
                            }
                            // 生成BeanDefinition
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setType(aClass);
                            // 单例or多例
                            if (aClass.isAnnotationPresent(Scope.class)) {
                                Scope scope = aClass.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scope.value());
                            } else {
                                beanDefinition.setScope("singleton");
                            }
                            this.beanDefinitionMap.put(beanName, beanDefinition);
                        }

                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }


    private void registerBeanPostProcessors() {
        // 加载内置的Bean后置处理器
        registerCommonBeanPostProcessor();
        // 生成后置处理器的bean，添加到容器
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            if (BeanPostProcessor.class.isAssignableFrom(beanDefinition.getType())) {
                BeanPostProcessor beanPostProcessor = (BeanPostProcessor) getBean(beanName);
                beanPostProcessorList.add(beanPostProcessor);
            }
        });
    }

    private void registerCommonBeanPostProcessor() {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setType(AnnotationAwareAspectJAutoProxyCreator.class);
        beanDefinition.setScope("singleton");
        String beanName = Introspector.decapitalize(AnnotationAwareAspectJAutoProxyCreator.class.getSimpleName());
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    private void preInstantiateSingletons() {
        // 将扫描到的单例 bean 创建出来放到单例池中
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            if (beanDefinition.isSingleton()) {
                getBean(beanName);
            }
        });
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        }
        if (beanDefinition.isSingleton()) {
            // 单例
            Object singletonObject = getSingleton(beanName, true);
            // 三处缓存都没找到，表示还未实例化
            if (singletonObject == null) {
                singletonObject = createBean(beanName, beanDefinition);
                this.singletonObjects.put(beanName, singletonObject);
                this.earlySingletonObjects.remove(beanName);
                this.singletonFactories.remove(beanName);
            }
            return singletonObject;
        } else {
            // 多例
            return createBean(beanName, beanDefinition);
        }
    }

    public Object getBean(Class<?> beanClass) {
        String name = Introspector.decapitalize(beanClass.getSimpleName());
        return getBean(name);
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        try {
            // 创建对象
            Object bean = createBeanInstance(beanDefinition);
            // 若是单例模式，将ObjectFactory放到三级缓存
            if (beanDefinition.isSingleton()) {
                this.singletonFactories.put(beanName, new ObjectFactory<Object>() {
                    // 若出现循环依赖，则提前创建bean代理对象
                    @Override
                    public Object getObject() throws RuntimeException {
                        Object exposedObject = bean;
                        for (BeanPostProcessor beanPostProcessor : AnnotationConfigApplicationContext.this.beanPostProcessorList) {
                            if (beanPostProcessor instanceof AnnotationAwareAspectJAutoProxyCreator) {
                                exposedObject = ((AnnotationAwareAspectJAutoProxyCreator) beanPostProcessor).getEarlyBeanReference(bean, beanName);
                            }
                        }
                        return exposedObject;
                    }
                });
            }
            // 依赖注入
            populateBean(beanDefinition, bean);
            // 初始化阶段
            Object initializeBean = initializeBean(beanName, bean);

            // 查看bean是否在二级缓存，如果在则说明发送循环依赖了
            // 这里主要作用是把代理对象返回（这里可能是代理对象，也有可能是原始对象）
            if (beanDefinition.isSingleton()) {
                Object earlySingletonReference = getSingleton(beanName, false);
                if (earlySingletonReference != null) {
                    initializeBean = earlySingletonReference;
                }
            }
            return initializeBean;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    private Object createBeanInstance(BeanDefinition beanDefinition) throws Throwable {
        Class<?> clazz = beanDefinition.getType();
        // 优先使用无参构造
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            }
        }
        // 没有无参构造，使用有参构造，随机选一个构造器
        Constructor<?> constructor = constructors[0];
        Object[] args = new Object[constructor.getParameterCount()];
        Parameter[] parameters = constructor.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            args[i] = getBean(parameter.getName());
        }
        return constructor.newInstance(args);
    }

    private void populateBean(BeanDefinition beanDefinition, Object bean) throws InvocationTargetException, IllegalAccessException {
        Class<?> type = beanDefinition.getType();
        // 解析字段上的 Autowired
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                field.set(bean, getBean(field.getName()));
            }
        }

        // 解析方法上的Autowired
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Autowired.class)) {
                int parameterCount = method.getParameterCount();
                if (parameterCount == 0) {
                    method.invoke(bean);
                    continue;
                }
                Object[] args = new Object[parameterCount];
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    args[i] = getBean(parameter.getName());
                }
                method.invoke(bean, args);
            }
        }

    }

    /**
     * 初始化阶段 aware -> 初始化前 -> 初始化 -> 初始化后
     */
    private Object initializeBean(String beanName, Object bean) {
        // aware回调
        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(this);
        }

        // 初始化前
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            bean = beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
        }

        // 初始化
        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }

        // 初始化后
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            bean = beanPostProcessor.postProcessAfterInitialization(bean, beanName);
        }
        return bean;
    }

    private Object getSingleton(String beanName, boolean allowEarlyReference) {
        // 一级缓存
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null) {
            // 二级缓存
            singletonObject = this.earlySingletonObjects.get(beanName);
            if (singletonObject == null && allowEarlyReference) {
                // 三级缓存
                ObjectFactory<?> objectFactory = this.singletonFactories.get(beanName);
                if (objectFactory != null) {
                    singletonObject = objectFactory.getObject();
                    this.earlySingletonObjects.put(beanName, singletonObject);
                    this.singletonFactories.remove(beanName);
                }
            }
        }
        return singletonObject;
    }

    public List<Class<?>> getAllBeanClass() {
        return this.beanDefinitionMap
                .values()
                .stream()
                .map(BeanDefinition::getType)
                .collect(Collectors.toList());
    }

    public void close() {
        destroySingletons();
    }

    private void destroySingletons() {
        // Clear all cached singleton instances in this registry.
        this.singletonObjects.clear();
        this.earlySingletonObjects.clear();
        this.singletonFactories.clear();
    }
}
