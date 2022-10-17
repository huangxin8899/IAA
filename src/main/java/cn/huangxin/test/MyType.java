package cn.huangxin.test;

import cn.huangxin.iaa.annotation.Autowired;
import cn.huangxin.iaa.annotation.Component;
import cn.huangxin.iaa.core.BeanNameAware;
import cn.huangxin.iaa.core.InitializingBean;
import cn.huangxin.test.service.DemoService;

/**
 * @author 黄鑫
 * @description MyType
 */
@Component
public class MyType implements BeanNameAware, InitializingBean {

    @Autowired
    private DemoService demoService;

    private String beanName;

    @Autowired
    public void test() {
        System.out.println("依赖注入阶段执行的无参方法:" + demoService);
    }

    @Override
    public void setBeanName(String beanName) {
        System.out.println("setBeanName方法执行，beanName：" + beanName);
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println(beanName + "执行初始化方法");
    }
}
