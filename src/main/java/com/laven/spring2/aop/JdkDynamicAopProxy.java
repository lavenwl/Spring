package com.laven.spring2.aop;

import com.laven.spring2.aop.aspect.Advice;
import com.laven.spring2.aop.intercept.MethodInvocation;
import com.laven.spring2.aop.support.AdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

public class JdkDynamicAopProxy implements InvocationHandler, AopProxy {
    private AdvisedSupport advised;

    public JdkDynamicAopProxy(AdvisedSupport config) {
        this.advised = config;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        Map<String, Advice> advices = config.getAdvices(method, null);
//        Object result;
//        try {
//            invokeAdvice(advices.get("before"));
//            result = method.invoke(this.config.getTarget(), args);
//            invokeAdvice(advices.get("after"));
//        } catch (Exception e) {
//            invokeAdvice(advices.get("afterThrow"));
//            throw e;
//        }

        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, this.advised.getTargetClass());
        MethodInvocation invocation = new MethodInvocation(proxy, this.advised.getTarget(), method, args, this.advised.getTargetClass(), chain);
        return invocation.proceed();
    }

//    private void invokeAdvice(Advice advice) {
//        try {
//            advice.getAdviceMethod().invoke(advice.getAspect());
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }

    public Object getProxy() {
        return this.getProxy(this.getClass().getClassLoader());
    }

    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, this.advised.getTargetClass().getInterfaces(), this);
    }
}
