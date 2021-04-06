package jcloudrabbitservice.demo.controller;

import jcloudrabbitservice.demo.service.MqPushService;
import org.springframework.beans.factory.annotation.Autowired;

public class MqController {
    @Autowired
    MqPushService mqPushService;

    public static void main(String[] args) {
        new MqController().mqPushService.send();
    }
}
