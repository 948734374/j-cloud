package com.cloud.jcloudrabbitconsumer.utils;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@RabbitListener(queues = "TestDirectQueue")
public class ReceiveMessageListener {

    @RabbitHandler
    public String input1(String message){
        System.out.println("消费者1，----->接收到的消息："+ message.toString());
        return message;
    }


    @RabbitHandler
    public Map input2(Map message){
        System.out.println("消费者2，----->接收到的消息："+ message.toString());
        return message;
    }


    @RabbitHandler
    public byte[] input3(byte[] message){
        System.out.println("消费者3，----->接收到的消息："+ message.toString());
        return message;
    }
}
