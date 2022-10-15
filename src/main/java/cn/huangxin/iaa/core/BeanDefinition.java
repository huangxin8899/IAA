package cn.huangxin.iaa.core;

/**
 * @author 黄鑫
 * @description BeanDefinition
 */
public class BeanDefinition {

    private Class<?> type;
    private String scope;

    public boolean isSingleton() {
        return "singleton".equals(scope);
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

}
