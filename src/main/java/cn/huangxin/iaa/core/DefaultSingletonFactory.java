package cn.huangxin.iaa.core;

/**
 * @author 黄鑫
 * @description DefaultSingletonFactory
 */
public class DefaultSingletonFactory implements ObjectFactory {

    private Object object;

    public DefaultSingletonFactory(Object object) {
        this.object = object;
    }

    @Override
    public Object getObject() throws RuntimeException {
        return this.object;
    }
}
