package cn.huangxin.iaa.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author 黄鑫
 * @description JDKProxyFactory
 */
public class JDKProxyFactory implements ProxyFactory, InvocationHandler {
    //要代理的原始对象
    private Object target;
    //切面对象集合
    private List<Advice> adviceList;

    @Override
    public Object createProxy(Object target, List<Advice> adviceList) {
        this.target = target;
        this.adviceList = adviceList;
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        for (Advice advice : this.adviceList) {
            if (AdviceEnum.BEFORE.equals(advice.getAdviceEnum())) {
                advice.getMethod().invoke(advice.getAspect(), args);
            }
        }
        Object beanProxy = method.invoke(this.target, args);
        for (Advice advice : this.adviceList) {
            if (AdviceEnum.AFTER.equals(advice.getAdviceEnum())) {
                advice.getMethod().invoke(advice.getAspect(), args);
            }
        }
        return beanProxy;
    }
}
