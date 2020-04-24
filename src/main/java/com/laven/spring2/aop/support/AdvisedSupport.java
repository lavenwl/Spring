package com.laven.spring2.aop.support;

import com.laven.spring2.aop.aspect.Advice;
import com.laven.spring2.aop.aspect.AfterReturningAdviceInterceptor;
import com.laven.spring2.aop.aspect.AspectJAfterThrowingAdvice;
import com.laven.spring2.aop.aspect.MethodBeforeAdviceInterceptor;
import com.laven.spring2.aop.config.AopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvisedSupport {
    private AopConfig config;
    // 目标实例
    private Object target;
    // 目标类对象
    private Class targetClass;
    private Pattern pointCutClassPattern;

//    private Map<Method, Map<String, Advice>> methodCache;

    private Map<Method, List<Object>> methodCache;

    public AdvisedSupport(AopConfig config) {
        this.config = config;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    // 解析配置文件的方法
    private void parse() {
        //把Spring的Excpress变成Java能够识别的正则表达式
        String pointCut = config.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");

        // 保存专门匹配Class的正则
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));


        // 享元的共享池
        methodCache = new HashMap<Method, List<Object>>();

        Pattern pointCutPattern = Pattern.compile(pointCut);
        try {
            // 获取切面类的对象及所有方法
            Class aspectClass = Class.forName(this.config.getAspectClass());
            Map<String, Method> aspectMethods = new HashMap<String, Method>();
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(), method);
            }

            // 循环处理目标类的方法
            for (Method method : this.targetClass.getMethods()) {
                String methodString = method.toString();
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pointCutPattern.matcher(methodString);
                if (matcher.matches()) {
//                    Map<String, Advice> advices = new HashMap<String, Advice>();
                    List<Object> advices = new LinkedList<Object>();
                    if (!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))) {
//                        advices.put("before", new Advice(aspectClass.newInstance(), aspectMethods.get(config.getAspectBefore())));
                        advices.add(new MethodBeforeAdviceInterceptor(aspectClass.newInstance(), aspectMethods.get(config.getAspectBefore())));
                    }
                    if(!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))){
//                        advices.put("after",new Advice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfter())));
                        advices.add(new AfterReturningAdviceInterceptor(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfter())));
                    }
                    if(!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))){
//                        Advice advice = new Advice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfterThrow()));
                        AspectJAfterThrowingAdvice advice = new AspectJAfterThrowingAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfterThrow()));
                        advice.setThrowName(config.getAspectAfterThrowingName());
                        advices.add(advice);
                    }

                    // 跟目标代理类的业务方法和Advices建立一对多个关联关系, 以便在Proxy类中获得
                    methodCache.put(method, advices);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    // 根据一个目标代理类的方法, 获取其对应的通知
//    public Map<String, Advice> getAdvices(Method method, Object o) throws NoSuchMethodException {
//        // 享元设计模式的应用
//        Map<String, Advice> cache = methodCache.get(method);
//        if (null == cache) {
//            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
//            cache = methodCache.get(m);
//            this.methodCache.put(m, cache);
//        }
//        return cache;
//    }

    // 根据一个目标代理类的方法, 获取其方法对应的通知
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws  Exception {
        // 从缓存中获取
        List<Object> cached = this.methodCache.get(method);
        // 缓存未命中, 则进行下一步处理
        if (cached == null) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cached = this.methodCache.get(m);
            // 存入缓存
            this.methodCache.put(m, cached);
        }
        return cached;
    }
}
