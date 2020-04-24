package com.laven.spring2.aop.aspect;

import com.laven.spring2.aop.intercept.MethodInterceptor;
import com.laven.spring2.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class AfterReturningAdviceInterceptor extends AbstractAspectJAdvice implements MethodInterceptor {
    private JoinPoint jp;

    public AfterReturningAdviceInterceptor(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    public void afterReturning(Method method, Object[] args, Object target) throws Throwable {
        this.invokeAdviceMethod(this.jp, null, null);
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        this.jp = methodInvocation;
        Object returnValue = methodInvocation.proceed();
        this.afterReturning(methodInvocation.getmethod(), methodInvocation.getArguments(), methodInvocation.getThis());
        return returnValue;
    }
}
