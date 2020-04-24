package com.laven.spring2.aop;

public interface AopProxy {
    Object getProxy();
    Object getProxy(ClassLoader classLoader);
}
