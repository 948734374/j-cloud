package jcloudprovider1.demo.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/provider-user/user")
public class UserController {
    @RequestMapping("/sayHello")
    public String sayhello(){
        return "I`m provider 1 ,Hello consumer!";
    }
    @RequestMapping("/sayHi")
    public String sayHi(){
        return "I`m provider 1 ,Hello consumer!";
    }
    @RequestMapping("/sayHaha")
    public String sayHaha(){
        return "I`m provider 1 ,Hello consumer!";
    }
}