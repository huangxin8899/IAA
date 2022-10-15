package cn.huangxin.test;

import cn.huangxin.iaa.annotation.ComponentScan;
import cn.huangxin.iaa.context.MyApplicationContext;
import cn.huangxin.iaa.core.AnnotationConfigApplicationContext;

/**
 * @author 黄鑫
 * @version 1.0
 * @date 2022/10/10 10:27
 * @description: TODO
 */
public class Test {



    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        System.out.println(applicationContext.getBean("demoService"));
    }
}
