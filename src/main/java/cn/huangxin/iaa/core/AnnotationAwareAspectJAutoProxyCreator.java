package cn.huangxin.iaa.core;

import cn.huangxin.iaa.annotation.After;
import cn.huangxin.iaa.annotation.Aspect;
import cn.huangxin.iaa.annotation.Before;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 黄鑫
 * @description Bean后置处理器事务实现类
 */
public class AnnotationAwareAspectJAutoProxyCreator implements BeanPostProcessor, ApplicationContextAware {

    private AnnotationConfigApplicationContext applicationContext;

    /**
     * 记录哪些 bean 尝试过提前创建代理，无论这个 bean 是否创建了代理增强，都记录下来，
     * 等到初始化阶段进行创建代理时，检查缓存，避免重复创建代理。
     * 存储的值就是 beanName
     */
    private final Set<Object> earlyProxyReferences = new HashSet<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
//        System.out.println(beanName + "执行内置Bean后置处理器postProcessBeforeInitialization");
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
//        System.out.println(beanName + "执行内置Bean后置处理器postProcessAfterInitialization");
        if (bean != null) {
            // earlyProxyReferences 中不包含当前 beanName，才创建代理
            if (!this.earlyProxyReferences.contains(beanName)) {
                return wrapIfNecessary(bean);
            } else {
                // earlyProxyReferences 中包含当前 beanName，不再重复进行代理创建，直接返回
                this.earlyProxyReferences.remove(beanName);
            }
        }
        return bean;
    }

    @Override
    public void setApplicationContext(AnnotationConfigApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Object getEarlyBeanReference(Object bean, String beanName) throws RuntimeException {
        this.earlyProxyReferences.add(beanName);
        return wrapIfNecessary(bean);
    }

    private Object wrapIfNecessary(Object bean) {
        Class<?> beanClass = bean.getClass();
        // 判断是否是切面通知
        if (isInfrastructureClass(beanClass)) {
            return bean;
        }
        // 查出所有的切面通知
        List<Advice> adviceList = findEligibleAdvisors();
        List<Advice> beanAdvices = adviceList.stream().filter(advice -> advice.getTargetClass().contains(beanClass.getSimpleName())).collect(Collectors.toList());
        for (Advice advice : adviceList) {
            if (advice.getTargetClass().contains(beanClass.getSimpleName())) {
                boolean isUseJDKProxy = isUseJDKProxy(beanClass, beanAdvices);
                Object proxyObject;
                if (isUseJDKProxy) {
                    // 使用jdk动态代理
                    proxyObject = jdkDynamicAopProxy(bean, beanAdvices);
                } else {
                    // 使用cglib动态代理
                    proxyObject = objenesisCglibAopProxy(bean, beanAdvices);
                }
                return proxyObject;
            }
        }
        return bean;
    }

    private List<Advice> findEligibleAdvisors() {
        List<Advice> adviceList = new ArrayList<>();
        List<Class<?>> allBeanClass = this.applicationContext.getAllBeanClass();
        for (Class<?> beanClass : allBeanClass) {
            for (Method method : beanClass.getDeclaredMethods()) {
                String pointCut = null;
                AdviceEnum adviceEnum = null;
                if (method.isAnnotationPresent(Before.class)) {
                    Before before = method.getDeclaredAnnotation(Before.class);
                    pointCut = before.value();
                    adviceEnum = AdviceEnum.BEFORE;
                }
                if (method.isAnnotationPresent(After.class)) {
                    After after = method.getDeclaredAnnotation(After.class);
                    pointCut = after.value();
                    adviceEnum = AdviceEnum.AFTER;
                }
                if (pointCut != null) {
                    int sep = pointCut.lastIndexOf(".");
                    String targetClass = pointCut.substring(0, sep);
                    String targetMethod = pointCut.substring(sep + 1);
                    adviceList.add(new Advice(targetClass, targetMethod, this.applicationContext.getBean(beanClass), adviceEnum, method));
                }
            }
        }
        return adviceList;
    }

    private boolean isInfrastructureClass(Class<?> aClass) {
        return aClass.isAnnotationPresent(Aspect.class);
    }

    private Object objenesisCglibAopProxy(Object bean, List<Advice> beanAdvices) {
        CglibProxyFactory proxyFactory = new CglibProxyFactory();
        return proxyFactory.createProxy(bean, beanAdvices);
    }

    private Object jdkDynamicAopProxy(Object bean, List<Advice> beanAdvices) {
        JDKProxyFactory proxyFactory = new JDKProxyFactory();
        return proxyFactory.createProxy(bean, beanAdvices);
    }

    private boolean isUseJDKProxy(Class<?> beanClass, List<Advice> beanAdvices) {
        Class<?>[] interfaces = beanClass.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            for (Method method : anInterface.getDeclaredMethods()) {
                for (Advice beanAdvice : beanAdvices) {
                    if (beanAdvice.getTargetMethod().equals(method.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
