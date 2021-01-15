package com.example.jcloudconsumer.controller;


import jcloud.jcloudfeign.feignclient.provide1Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consumer/user")
public class UserController {
    @Autowired
    provide1Client provide1Client;

    @RequestMapping("/sayHello")
    public String sayhello(){
        return provide1Client.sayhello();
    }

}