package jcloudrabbitservice.demo.service.impl;

import jcloudrabbitservice.demo.service.MqPushService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EnableBinding(Source.class)
public class MqPushServiceImpl implements MqPushService {

    /**
     * 消息发送管道
     */
    @Autowired
    private MessageChannel output;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void send() {
        String serial = UUID.randomUUID().toString();

        System.out.println("serial = " + serial);
        rabbitTemplate.convertAndSend("TestDirectExchange",serial,"500000");

        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "test message, hello!";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String,Object> map=new HashMap<>();
        map.put("messageId",messageId);
        map.put("messageData",messageData);
        map.put("createTime",createTime);

        rabbitTemplate.convertAndSend("TestDirectExchange", "TestDirectRouting", map);
    }
}
