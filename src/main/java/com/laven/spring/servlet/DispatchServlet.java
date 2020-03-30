package com.laven.spring.servlet;

import com.laven.spring.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class DispatchServlet extends HttpServlet {
    // 全局配置文件, 用来存储配置信息
    private Properties contextConfig = new Properties();

    // 所有需要控制的类"名"的集合
    private List<String> classNames = new ArrayList<String>();

    // 模仿IOC容器, 存储所有实例对象; key: 类名首字母小写, value: 对应的实例.
    private Map<String, Object> ioc = new HashMap<String, Object>();

    // 请求url与方法的映射集合
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 6. 委派, 根据url找到具体对应的method, 并通过response返回
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception, Detail: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        if(!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 not Found Mehod!");
            return;
        }

        Map<String, String[]> params = req.getParameterMap();
        Method method = this.handlerMapping.get(url);

        // 获取形参列表
        Class<?> [] parameterTypes = method.getParameterTypes();
        Object [] paramValues = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            if(parameterType == HttpServletRequest.class) {
                paramValues[i] = req;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = resp;
            } else if (parameterType == String.class) {
                // 同过运行时的状态去拿
                Annotation[] [] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for (Annotation a : pa[i]) {
                        if(!(a instanceof RequestParam)) {continue;}
                        String paramName = ((RequestParam) a).value();
                        if("".equals(paramName.trim())) {continue;}
                        String value = Arrays.toString(params.get(paramName))
                                .replaceAll("\\[|\\]", "")
                                .replaceAll("\\s+", ",");
                        paramValues[i] = value;
                    }
                }
            }
        }
        // 获取类属性对应的实例
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName), paramValues);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1. 加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        // 2. 扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        // 3. 初始化IOC容器, 将扫描到的相关类实例化, 到IOC容器中
        doInstance();
        // AOP
        // 4. 依赖注入DI操作
        doAutowired();
        // 5. 初始化HandlerMapping
        doHandlerMapping();

        System.out.println("手写Spring初始化完成");
    }

    private void doHandlerMapping() {
        if(ioc.isEmpty()) {return;}
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if(!clazz.isAnnotationPresent(Controller.class)) {continue;}

            // 提取clss上的url
            String baseUrl = "";
            if(clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl = requestMapping.value();
            }

            // 只获取public方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(RequestMapping.class)) {continue;}
                // 提取方法上配置的url
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);

                System.out.println("添加映射方法: " + url + " " + method);
            }
        }
    }

    private void doAutowired() {
        if(ioc.isEmpty()) { return; }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                if(!field.isAnnotationPresent(Autowired.class)) { continue; }
                Autowired autowired = field.getAnnotation(Autowired.class);

                // 如果用户没有自定义beanName, 就默认根据类型注入
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                // 暴力访问
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) { return; }

        try {
            for (String className: classNames) {
                Class<?> clazz = Class.forName(className);

                if(clazz.isAnnotationPresent(Controller.class)) {
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    // 1.如果多个包下出现相同的类名, 只能自己起一个全局唯一的名字(自定义命名)
                    String beanName = clazz.getAnnotation(Service.class).value();
                    if("".equals(beanName)) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    // 2. 默认的类名首字母小写
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    // 3. 如果是接口: 1)判断有多少个实现类; 2)如果有一个默认选择这个实现类; 3)如果有多个, 抛出异常
                    // ?? 理解这一步接口的操作, 是否与上面的ioc.put重复操作
                    for (Class<?> i : clazz.getInterfaces()) {
                        if(ioc.containsKey(i.getName())) {
                            throw new Exception("The" + i.getName() + " is exists!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
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
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
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
}
