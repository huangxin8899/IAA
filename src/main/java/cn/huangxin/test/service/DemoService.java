package cn.huangxin.test.service;

import cn.huangxin.iaa.annotation.Autowired;
import cn.huangxin.iaa.annotation.Component;
import cn.huangxin.test.MyType;

/**
 * @author 黄鑫
 * @description DemoService
 */
@Component
public class DemoService {

    @Autowired
    private MyType myType;
}
