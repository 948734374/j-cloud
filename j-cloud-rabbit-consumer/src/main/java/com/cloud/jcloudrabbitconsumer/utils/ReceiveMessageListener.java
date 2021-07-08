package com.cloud.jcloudrabbitconsumer.utils;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


@Component
@RabbitListener/*(queues = "TestDirectQueue")*/
public class ReceiveMessageListener {

    @RabbitHandler
    public void input1(String message, Channel channel, Message message1) throws IOException {
        channel.basicAck(message1.getMessageProperties().getDeliveryTag(),true);
        System.out.println("消费者1，----->接收到的消息："+ message.toString());

    }


    @RabbitHandler
    public void input2(Map message){
        System.out.println("消费者2，----->接收到的消息："+ message.toString());

    }


    @RabbitHandler
    public void input3(byte[] message){
        System.out.println("消费者3，----->接收到的消息："+ message.toString());

    }
}
