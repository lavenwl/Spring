package com.laven.spring2.aop.intercept;

import com.laven.spring2.aop.aspect.JoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodInvocation implements JoinPoint {
    protected final Object proxy;

    protected final Object target;

    protected final Method method;

    protected Object[] arguments = new Object[0];

    private final Class<?> targetClass;

    private Map<String, Object> userAttributes = new HashMap<String, Object>();

    protected final List<?> interceptorsAndDynamicMethodMatchers;

    private int currentInterceptorIndex = -1;

    public MethodInvocation (
        Object proxy,
        Object target,
        Method method,
        Object[] arguments,
        Class<?> targetClass,
        List<Object> interceptorsAndDynamicMethodMatchers )
    {
        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = method;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    public Object proceed() throws Throwable {
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return method.invoke(this.target, this.arguments);
        }

        Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);


        // 如果要动态匹配JoinPoint
        if (interceptorOrInterceptionAdvice instanceof MethodInterceptor) {
            MethodInterceptor methodInterceptor = (MethodInterceptor) interceptorOrInterceptionAdvice;
            return methodInterceptor.invoke(this);
        } else {
            return proceed();
        }
    }

    public Object[] getArguments() {
        return this.arguments;
    }

    public Method getmethod() {
        return null;
    }

    public Object[] getThis() {
        return new Object[0];
    }

    public void setUserAttribute(String key, Object value) {

    }

    public Object getUserAttribute(String key) {
        return null;
    }
//    protected final Object proxy;

}
