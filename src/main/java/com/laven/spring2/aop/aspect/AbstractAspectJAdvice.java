package com.laven.spring2.aop.aspect;

import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@Data
public class AbstractAspectJAdvice implements Advice {
    // 具体切面对象实例
    private Object aspect;
    // 对应需要执行的方法
    private Method adviceMethod;
    private String throwName;

    public AbstractAspectJAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }

    protected Object invokeAdviceMethod(JoinPoint jp, Object returnValue, Throwable t) throws InvocationTargetException, IllegalAccessException {
        Class<?> [] paramTypes = this.adviceMethod.getParameterTypes();
        if (null == paramTypes || paramTypes.length == 0) {
            return this.adviceMethod.invoke(this.aspect);
        } else {
            Object [] args = new Object[paramTypes.length];
            for ( int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i] == JoinPoint.class) {
                    args[i] = jp;
                } else if (paramTypes[i] == Throwable.class) {
                    args[i] = t;
                } else if ( paramTypes[i] == Object.class) {
                    args[i] = returnValue;
                }
            }
            return this.adviceMethod.invoke(aspect, args);
        }
    }
}
