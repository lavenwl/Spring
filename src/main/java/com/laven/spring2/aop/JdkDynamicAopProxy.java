package com.laven.spring2.aop;

import com.laven.spring2.aop.aspect.Advice;
import com.laven.spring2.aop.support.AdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class JdkDynamicAopProxy implements InvocationHandler {
    private AdvisedSupport config;

    public JdkDynamicAopProxy(AdvisedSupport config) {
        this.config = config;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, Advice> advices = config.getAdvices(method, null);
        Object result;
        try {
            invokeAdvice(advices.get("before"));
            result = method.invoke(this.config.getTarget(), args);
            invokeAdvice(advices.get("after"));
        } catch (Exception e) {
            invokeAdvice(advices.get("afterThrow"));
            throw e;
        }

        return result;
    }

    private void invokeAdvice(Advice advice) {
        try {
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.config.getTargetClass().getInterfaces(), this);
    }
}
