package cn.huangxin.test;

import cn.huangxin.iaa.annotation.Autowired;
import cn.huangxin.iaa.annotation.Component;
import cn.huangxin.test.service.DemoService;

/**
 * @author 黄鑫
 * @description MyType
 */
@Component
public class MyType {

    @Autowired
    private DemoService demoService;

    @Autowired
    public void test() {
        System.out.println("依赖注入阶段执行的无参方法");
    }

    @Autowired
    public void test2() {
        System.out.println(demoService);
    }
}
