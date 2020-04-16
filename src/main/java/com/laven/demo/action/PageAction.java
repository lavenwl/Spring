package com.laven.demo.action;

import com.laven.demo.service.IQueryService;
import com.laven.spring2.annotation.Autowired;
import com.laven.spring2.annotation.RequestParam;
import com.laven.spring2.annotation.Controller;
import com.laven.spring2.annotation.RequestMapping;
import com.laven.spring2.webmvc.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/")
public class PageAction {

    @Autowired
    IQueryService queryService;

    @RequestMapping("first.html")
    public ModelAndView query(@RequestParam("teacher") String teacher) {
        String result = queryService.query(teacher);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new ModelAndView("first.html", model);
    }

    @Override
    public String toString() {
        return "PageAction{" +
                "queryService=" + queryService +
                '}';
    }
}
