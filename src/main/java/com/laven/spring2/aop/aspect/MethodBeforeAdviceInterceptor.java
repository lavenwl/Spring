package com.laven.spring2.aop.aspect;

import com.laven.spring2.aop.intercept.MethodInterceptor;
import com.laven.spring2.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class MethodBeforeAdviceInterceptor extends AbstractAspectJAdvice implements MethodInterceptor {
    private JoinPoint jp;

    public MethodBeforeAdviceInterceptor(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    public void before(Method method, Object[] args, Object target) throws Throwable {
        this.invokeAdviceMethod(this.jp, null, null);
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        jp = methodInvocation;
        this.before(methodInvocation.getmethod(), methodInvocation.getArguments(), methodInvocation.getThis());
        return methodInvocation.proceed();
    }
}
