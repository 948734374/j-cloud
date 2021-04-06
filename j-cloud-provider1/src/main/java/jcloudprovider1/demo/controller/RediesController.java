package jcloudprovider1.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/provider-user/redies")
public class RediesController {

@Autowired
    RedisTemplate<String,Object> redisTemplate;



}
