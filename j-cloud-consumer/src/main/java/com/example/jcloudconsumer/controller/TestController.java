package com.example.jcloudconsumer.controller;


import com.cloud.jcloudcommon.utils.RedisUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/consumer/test")
public class TestController {

    @Resource
    RedisUtil redisUtil;

    @RequestMapping("/testRedies")
    public String sayhello(){
        return redisUtil.get("111").toString();
    }
}
