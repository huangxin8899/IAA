package cn.huangxin.iaa.core;

import java.lang.reflect.Method;

/**
 * @author 黄鑫
 * @description Aspect实体，描述的是切面的信息
 * 包括连接点（目标对象类与类名，目标对象方法与方法名）和通知（前置通知和后置通知）
 */
public class Advice {

    private String targetClass;
    private String targetMethod;
    private Object aspect;
    private AdviceEnum adviceEnum;
    private Method method;

    public Advice(String targetClass, String targetMethod, Object aspect, AdviceEnum adviceEnum, Method method) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.aspect = aspect;
        this.adviceEnum = adviceEnum;
        this.method = method;
    }

    public String getTargetClass() { return targetClass; }
    public String getTargetMethod() { return targetMethod; }
    public Object getAspect() { return aspect; }
    public AdviceEnum getAdviceEnum() { return adviceEnum; }
    public Method getMethod() { return method; }
}
