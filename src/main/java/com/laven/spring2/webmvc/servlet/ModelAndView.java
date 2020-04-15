package com.laven.spring2.webmvc.servlet;

import java.util.Map;

public class ModelAndView {
    // 模型或视图的名字
    private String viewName;
    // 具体的模型
    private Map<String, ?> model;

    public ModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
