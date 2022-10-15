package cn.huangxin.test;

import cn.huangxin.iaa.core.AnnotationConfigApplicationContext;

/**
 * @author 黄鑫
 * @description: 测试类
 */
public class Test {



    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        System.out.println(applicationContext.getBean("demoService"));
    }
}
