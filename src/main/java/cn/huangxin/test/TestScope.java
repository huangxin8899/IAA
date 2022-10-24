package cn.huangxin.test;

import cn.huangxin.iaa.annotation.Component;
import cn.huangxin.iaa.annotation.Scope;
import cn.huangxin.iaa.core.ScopeType;

/**
 * @author 黄鑫
 * @description TestScope
 */
@Component
@Scope(ScopeType.PROTOTYPE)
public class TestScope {
}
