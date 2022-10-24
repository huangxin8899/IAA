package cn.huangxin.iaa.core;

/**
 * @author 黄鑫
 * @description ObjectFactory
 */
@FunctionalInterface
public interface ObjectFactory<T> {

    T getObject() throws RuntimeException;
}
