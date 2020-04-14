package com.laven.spring2.beans;

public class BeanWrapper {
    private Object wrapperInstance;

    private Class<?> wrapperedClass;

    public BeanWrapper(Object instance) {
        this.wrapperInstance = instance;
        this.wrapperedClass = instance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Class<?> getWrapperedClass() {
        return wrapperedClass;
    }
}
