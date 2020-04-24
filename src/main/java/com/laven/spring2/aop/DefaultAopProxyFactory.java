package com.laven.spring2.aop;

import com.laven.spring2.aop.support.AdvisedSupport;

public class DefaultAopProxyFactory {
    public AopProxy createAopProxy(AdvisedSupport config) {
        Class targetClass = config.getTargetClass();
        if (targetClass.getInterfaces().length > 0) {
            return new JdkDynamicAopProxy(config);
        }
        return new CglibAopProxy();
    }
}
