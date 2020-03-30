package com.laven.demo.action;

import com.laven.demo.service.IDemoService;
import com.laven.spring.annotation.Autowired;
import com.laven.spring.annotation.Controller;
import com.laven.spring.annotation.RequestMapping;
import com.laven.spring.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/demo")
public class DemoAction {
    @Autowired private IDemoService demoService;

    @RequestMapping("query")
    public void query(HttpServletResponse req, HttpServletResponse resp, @RequestParam("name") String name) {
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @RequestParam("a") Double a, @RequestParam("b") Double b) {
        try {
            resp.getWriter().write(a + " + " + b + " = " + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/sub")
    public void sub(HttpServletRequest req, HttpServletResponse resp,
                    @RequestParam("a") Double a, @RequestParam("b") Double b) {
        try {
            resp.getWriter().write(a + " - " + b + " = " + (a - b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("remove")
    public String remove(@RequestParam("id") Integer id) {
        return "removed" + id;
    }

}
