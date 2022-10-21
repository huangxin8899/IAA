package cn.huangxin.iaa.core;

/**
 * @author 黄鑫
 * @description ApplicationContextAware
 */
public interface ApplicationContextAware {

    void setApplicationContext(AnnotationConfigApplicationContext applicationContext);
}
