package cn.huangxin.iaa.core;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author 黄鑫
 * @description CglibProxyFactory
 */
public class CglibProxyFactory implements ProxyFactory, MethodInterceptor {
    //要代理的原始对象
    private Object target;
    //切面对象集合
    private List<Advice> adviceList;

    @Override
    public Object createProxy(Object target, List<Advice> adviceList){
        this.target = target;
        this.adviceList = adviceList;
        //1-Enhancer类是CGLib中的一个字节码增强器，它可以方便的对你想要处理的类进行扩展
        Enhancer enhancer=new Enhancer();
        //2-将被代理类设置成父类
        enhancer.setSuperclass(this.target.getClass());
        //3-设置拦截器
        enhancer.setCallback(this);
        //4-动态生成一个代理类
        Object objProxy = enhancer.create();
        return objProxy;

    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        for (Advice advice : adviceList) {
            if (AdviceEnum.BEFORE.equals(advice.getAdviceEnum())) {
                advice.getMethod().invoke(advice.getAspect(), args);
            }
        }
        Object proxy = methodProxy.invokeSuper(object, args);
        for (Advice advice : adviceList) {
            if (AdviceEnum.AFTER.equals(advice.getAdviceEnum())) {
                advice.getMethod().invoke(advice.getAspect(), args);
            }
        }
        return proxy;
    }
}
