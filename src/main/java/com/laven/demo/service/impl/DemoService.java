package com.laven.demo.service.impl;

import com.laven.demo.service.IDemoService;
import com.laven.spring.annotation.Service;

@Service
public class DemoService implements IDemoService {
    public String get(String name) {
        return "My name is " + name + ", from Service.";
    }
}
