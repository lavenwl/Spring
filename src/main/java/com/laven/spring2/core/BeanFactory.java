package com.laven.spring2.core;

public interface BeanFactory {
    public Object getBean(Class beanClass);

    public Object getBean(String beanName);
}
