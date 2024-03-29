package jcloudrabbitservice.demo.service.impl;

import jcloudrabbitservice.demo.service.MqPushService;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MqPushServiceImpl implements MqPushService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    Source source;
    @Autowired
    RabbitAdmin rabbitAdmin;

    @Override
    public void send() {

        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "test message, hello!";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String,Object> map=new HashMap<>();
        map.put("messageId",messageId);
        map.put("messageData",messageData);
        map.put("createTime",createTime);

        rabbitTemplate.convertAndSend("TestDirectExchange", "TestDirectRouting", map);
    }

    @Override
    public void send1() {
        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "test message, hello!";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String,Object> map=new HashMap<>();
        map.put("messageId",messageId);
        map.put("messageData",messageData);
        map.put("createTime",createTime);

        Object o = rabbitTemplate.convertSendAndReceive("TestDirectExchange", "TestDirectRouting", map);
    }

}
