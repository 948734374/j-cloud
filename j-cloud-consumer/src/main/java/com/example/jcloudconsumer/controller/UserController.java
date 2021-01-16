package com.example.jcloudconsumer.controller;


import jcloud.jcloudfeign.feignclient.DemoFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consumer/user")
public class UserController {
//    @Autowired
//    provide1Client provide1Client;
    //@Resource
//    @Autowired
//    private Provide1Client provide1Client;

    @Autowired
    private DemoFeign demoFeign;


    /*@RequestMapping("/sayHello")
    public String sayhello(){
        return provide1Client.sayhello();
    }*/

    public void test(){
        //String sayhello = provide1Client.sayhello();
    }



}