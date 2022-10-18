package cn.huangxin.iaa.core;

/**
 * @author 黄鑫
 * @description ObjectFactory
 */
public interface ObjectFactory<T> {

    T getObject() throws RuntimeException;
}
