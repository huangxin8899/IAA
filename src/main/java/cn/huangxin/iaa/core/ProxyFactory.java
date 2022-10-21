package cn.huangxin.iaa.core;

import java.util.List;

/**
 * @author 黄鑫
 * @description ProxyFactory
 */
public interface ProxyFactory {
    public Object createProxy(Object target, List<Advice> adviceList);
}
