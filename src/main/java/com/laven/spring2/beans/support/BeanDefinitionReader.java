package com.laven.spring2.beans.support;

import com.laven.spring2.annotation.Controller;
import com.laven.spring2.annotation.Service;
import com.laven.spring2.beans.config.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BeanDefinitionReader {

    private List<String> registryBeanClasses = new ArrayList<String>();

    private Properties contextConfig = new Properties();

    public BeanDefinitionReader(String... configLocation) {
        // 1. 加载配置文件
        doLoadConfig(configLocation[0]);

        // 2. 扫描配置文件配置的包
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    private void doScanner(String scanPackage) {
        // ?? 了解URL对象
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());

        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                // ?? 为什么 "." 与 "/" 切换
                doScanner(scanPackage + "." + file.getName());
            } else {
                if(!file.getName().endsWith(".class")) { continue; }
                // 全类名 = 包名 + . + 类名
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                registryBeanClasses.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replaceAll("classpath:", ""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<BeanDefinition> loadBeanDefinitions() {
        List<BeanDefinition> result = new ArrayList<BeanDefinition>();
        try {
            for (String className : registryBeanClasses) {
                Class<?> beanClass = Class.forName(className);

                if(beanClass.isAnnotationPresent(Controller.class)) {
                    result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));
                } else if (beanClass.isAnnotationPresent(Service.class)) {
                    // 1.如果多个包下出现相同的类名, 只能自己起一个全局唯一的名字(自定义命名)
                    String beanName = beanClass.getAnnotation(Service.class).value();
                    if("".equals(beanName)) {
                        beanName = toLowerFirstCase(beanClass.getSimpleName());
                    }
                    // 2. 默认的类名首字母小写
                    result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));
                    // 3. 如果是接口: 1)判断有多少个实现类; 2)如果有一个默认选择这个实现类; 3)如果有多个, 抛出异常
                    // ?? 理解这一步接口的操作, 是否与上面的ioc.put重复操作
                    for (Class<?> i : beanClass.getInterfaces()) {
                        if(result.contains(i.getName())) {
                            throw new Exception("The" + i.getName() + " is exists!");
                        }
                        result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                    }
                } else {
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    private BeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    // 首字母小写
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig() {
        return this.contextConfig;
    }
}
