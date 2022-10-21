package cn.huangxin.test;

import cn.huangxin.iaa.annotation.After;
import cn.huangxin.iaa.annotation.Aspect;
import cn.huangxin.iaa.annotation.Before;
import cn.huangxin.iaa.annotation.Component;

/**
 * @author 黄鑫
 * @description AopTest
 */
@Aspect
@Component
public class AopTest {


    @Before("cn.huangxin.test.MyType.test1")
    public void before() {
        System.out.println("执行before");
    }

    @After("cn.huangxin.test.MyType.test1")
    public void after() {
        System.out.println("执行after");
    }
}
