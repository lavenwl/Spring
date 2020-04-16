package com.laven.spring2.webmvc.servlet;

import com.laven.spring2.annotation.*;
import com.laven.spring2.context.ApplicationContext;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchServlet extends HttpServlet {
    // 应用执行上下文, 相当于Spring IOC容器的工厂类
    private ApplicationContext applicationContext;

    // 请求url与方法的映射集合
    private List<HandlerMapping> handlerMappings = new ArrayList<HandlerMapping>();

    // 参数适配器集合
    private Map<HandlerMapping, HandlerAdapter> handlerAdapters = new HashMap<HandlerMapping, HandlerAdapter>();

    // 视图解析器
    private List<ViewResolver> viewResolvers = new ArrayList<ViewResolver>();

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
            try {
                processDispatcherResult(req, resp, new ModelAndView("500"));
            } catch (Exception ex) {
                ex.printStackTrace();
                resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void processDispatcherResult(HttpServletRequest req, HttpServletResponse resp, ModelAndView modelAndView) throws Exception {
        if (null == modelAndView) {return;}
        if (this.viewResolvers.isEmpty()) {return;}

        for (ViewResolver viewResolver : this.viewResolvers) {
            View view = viewResolver.resolveViewName(modelAndView.getViewName());
            // 直接往浏览器输出
            view.render(modelAndView.getModel(), req, resp);
            return;
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 1. 通过URL获取一个HandlerMapping
        HandlerMapping handlerMapping = getHandler(req);
        if (handlerMapping == null) {
            processDispatcherResult(req, resp, new ModelAndView("404"));
            return;
        }
        // 2. 根据一个HandlerMapping获取一个HandlerAdaptor
        HandlerAdapter handlerAdapter = getHandlerAdapter(handlerMapping);
        // 3. 解析某一个方法的形参和返回值之后, 统一封装为ModelAndView对象
        ModelAndView modelAndView = handlerAdapter.handler(req, resp, handlerMapping);
        // 4. 把ModelAndView变成一个ViewResolver
        processDispatcherResult(req, resp, modelAndView);
    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handlerMapping) {
        if (this.handlerAdapters.isEmpty()) { return null; }
        return this.handlerAdapters.get(handlerMapping);
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) { return null; }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        for (HandlerMapping handlerMapping : handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            if (!matcher.matches()) { continue; }
            return handlerMapping;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 初始化Spring核心IOC容器
        applicationContext = new ApplicationContext(config.getInitParameter("contextConfigLocation"));

        // 初始化九大组件
        initStrategies(applicationContext);

        System.out.println("手写Spring初始化完成");
    }

    private void initStrategies(ApplicationContext context) {
        // 多文件上传的组件
//        initMultipartResolver(context);
        // 初始化本地语言环境
//        initLocaleResolver(context);
        // 初始化模板处理器
//        initThemeResolver(context);
        // 初始化请求分发处理器
        initHandlerMappings(context);
        // 初始化参数适配器
        initHandlerAdapter(context);
        // 初始化异常拦截器
//        initHandlerExceptionResolvers(context);
        // 初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        // 初始化视图转换器
        initViewResolvers(context);
        // 初始化FlashMap管理器
//        initFlashMapManager(context);
    }

    private void initHandlerAdapter(ApplicationContext context) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new HandlerAdapter());
        }
    }

    private void initHandlerMappings(ApplicationContext context) {
        if(this.applicationContext.getBeanDefinitionCount() == 0) {return;}
        for (String beanName : this.applicationContext.getBeanDefinitionNames()) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();

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
//                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new HandlerMapping(pattern, method, instance));

                System.out.println("添加映射方法: " + regex + " " + method);
            }
        }
    }

    private void initViewResolvers(ApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        // 每一个页面设置一个页面解析器, 并缓存到viewResolvers对象里面.
        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new ViewResolver(templateRoot));
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
