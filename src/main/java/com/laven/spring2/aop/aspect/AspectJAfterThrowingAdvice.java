package com.laven.spring2.aop.aspect;

import com.laven.spring2.aop.intercept.MethodInterceptor;
import com.laven.spring2.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class AspectJAfterThrowingAdvice extends AbstractAspectJAdvice implements MethodInterceptor {
    private String throwName;

    public AspectJAfterThrowingAdvice(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        try {
            return methodInvocation.proceed();
        } catch (Throwable ex) {
            invokeAdviceMethod(methodInvocation, null, ex.getCause());
            throw ex;
        }
    }

    public void setThrowName(String throwName) {
        this.throwName = throwName;
    }
}
