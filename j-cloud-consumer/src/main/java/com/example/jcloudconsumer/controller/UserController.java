package com.example.jcloudconsumer.controller;


import jcloud.jcloudfeign.feignclient.DemoFeign;
import jcloud.jcloudfeign.feignclient.Provide1Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/consumer/user")
public class UserController {
    @Resource
    private Provide1Client provide1Client;

    @Autowired
    private DemoFeign demoFeign;

    @RequestMapping("/sayHello")
    public String sayhello(){
        return provide1Client.sayhello();
    }

    @RequestMapping("/sayHi")
    public String sayHi(){
        return "I`m consumer 1";
    }
}