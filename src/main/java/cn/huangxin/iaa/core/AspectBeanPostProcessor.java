package cn.huangxin.iaa.core;

import cn.huangxin.iaa.annotation.After;
import cn.huangxin.iaa.annotation.Aspect;
import cn.huangxin.iaa.annotation.Before;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 黄鑫
 * @description Bean后置处理器事务实现类
 */
public class AspectBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private AnnotationConfigApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println(beanName + "执行内置Bean后置处理器postProcessBeforeInitialization");
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println(beanName + "执行内置Bean后置处理器postProcessAfterInitialization");
        return wrapIfNecessary(bean);
    }

    @Override
    public void setApplicationContext(AnnotationConfigApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private Object wrapIfNecessary(Object bean) {

        List<Advice> adviceList = new ArrayList<>();
        List<Class<?>> allBeanClass = this.applicationContext.getAllBeanClass();
        for (Class<?> beanClass : allBeanClass) {
            if (beanClass.isAnnotationPresent(Aspect.class)) {
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
        }
        Class<?> beanClass = bean.getClass();
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
