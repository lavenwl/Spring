package com.laven.spring2.context;

import com.laven.spring2.annotation.Controller;
import com.laven.spring2.annotation.Autowired;
import com.laven.spring2.annotation.Service;
import com.laven.spring2.beans.BeanWrapper;
import com.laven.spring2.beans.config.BeanDefinition;
import com.laven.spring2.beans.support.BeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 职责: 完成Bean的创建, 和DI
 */
public class ApplicationContext {
    // 读取配置文件, 扫描配置的包内所有的类, 并返回包装好的BeanDefinition
    private BeanDefinitionReader reader;
    // 以工厂方法获取实例的简单名字为KEY, 以改类的定义类为VALUE, 缓存对象
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<String, BeanDefinition>();
    // bean实例的缓存
    public Map<String, BeanWrapper> factoryBeanInstanceCache = new HashMap<String, BeanWrapper>();

    private Map<String, Object> factoryBeanObjectCache = new HashMap<String, Object>();


    public ApplicationContext(String... configLocations) {
        try {
            // 1. 加载配置文件
            reader = new BeanDefinitionReader(configLocations);
            // 2. 解析配置文件, 封装成BeanDefinition
            List<BeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
            // 3. 将BeanDefinition缓存起来
            doRegistDefinition(beanDefinitions);
            // 4. 配置自动注入阶段
            doAutowrited();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    private void doAutowrited() {
        // 调用getBean()
        // 这一步, 所有的Bean还没有真正的实例化, 只是配置阶段
        for(Map.Entry<String, BeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            getBean(beanName);
        }
    }

    // Bean 的实例化, DI是从这个方法开始的
    public Object getBean(String beanName) {
        // 1. 先拿到BeanDefination配置信息
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        // 2. 反射实例化newInstance()
        Object instance = instantiateBean(beanName, beanDefinition);
        // 3. 封装成一个叫做BeanWrapper
        BeanWrapper beanWrapper = new BeanWrapper(instance);
        // 4. 保存到IOC容器
        factoryBeanInstanceCache.put(beanName, beanWrapper);
        // 5. 执行依赖注入
        populateBean(beanName, beanDefinition, beanWrapper);

        return beanWrapper.getWrapperInstance();
    }

    private void populateBean(String beanName, BeanDefinition beanDefinition, BeanWrapper beanWrapper) {
        // 可能涉及到循环依赖的问题
        // 使用两个缓存, 循环两次
        // 1. 把第一次读取结果为空的BeanDefinition存到第一个缓存
        // 2. 等第一次循环后, 第二次循环再检查第一次的缓存, 再进行赋值
        Object instance = beanWrapper.getWrapperInstance();
        Class<?> clazz = beanWrapper.getWrapperedClass();

        // Spring 中判断的是 @Component注解
        if (!(clazz.isAnnotationPresent(Controller.class) || (clazz.isAnnotationPresent(Service.class)))) {
            return;
        }

        // 吧所有的包括private/ protected / default / public 修饰的字段取出来
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Autowired.class)) { continue; }

            Autowired autowired = field.getAnnotation(Autowired.class);

            // 如果用户没有自定义beanName 就默认根据类型注入
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            // 暴力访问
            field.setAccessible(true);

            try {
                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null) {
                    continue;
                }
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

    }

    // 创建真正的实例对象
    private Object instantiateBean(String beanName, BeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try{
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();
            this.factoryBeanObjectCache.put(beanName, instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }


    private void doRegistDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is Exists!");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
