package com.example.springbootdocker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class HelloWorldController {

    @Autowired
    @Qualifier("jwksRestTemplate")
    private RestTemplate jwsUtilsRestTemplate;

    @RequestMapping("/hello")
    public String hello() {
        return "Hi Krishna!";
    }
}
