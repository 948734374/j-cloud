package jcloudrabbitservice.demo.controller;

import jcloudrabbitservice.demo.service.MqPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class MqController {
    @Autowired
    MqPushService mqPushService;

    @RequestMapping("/testMq")
    public void sayhello(){
        mqPushService.send();
    }
}
