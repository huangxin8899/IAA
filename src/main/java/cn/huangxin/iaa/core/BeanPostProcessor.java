package cn.huangxin.iaa.core;

/**
 * @author 黄鑫
 * @description BeanPostProcessor bean后置处理器
 * AOP就是在这里进行
 */
public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
