package com.laven.spring2.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;
@Data
public class Advice {
    // 具体切面对象实例
    private Object aspect;
    // 对应需要执行的方法
    private Method adviceMethod;
    private String throwName;

    public Advice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }
}
