package com.laven.spring2.beans.config;

public class BeanDefinition {
    // 使用工厂方法获取实例使用的简单的名字
    private String factoryBeanName;
    // 改对象具体的类的加载路径
    private String beanClassName;

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}
