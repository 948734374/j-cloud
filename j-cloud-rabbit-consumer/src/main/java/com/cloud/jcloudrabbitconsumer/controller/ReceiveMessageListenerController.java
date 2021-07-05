package com.cloud.jcloudrabbitconsumer.controller;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@RabbitListener(queues = "TestDirectQueue")
public class ReceiveMessageListenerController {

    @RabbitHandler
    public void input(Object message){
        System.out.println("消费者，----->接收到的消息："+ message.toString());
    }
}
