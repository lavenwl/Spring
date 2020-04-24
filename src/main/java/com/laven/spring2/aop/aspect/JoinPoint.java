package com.laven.spring2.aop.aspect;

import java.lang.reflect.Method;

public interface JoinPoint {
    Method getmethod();

    Object[] getThis();

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);
}
