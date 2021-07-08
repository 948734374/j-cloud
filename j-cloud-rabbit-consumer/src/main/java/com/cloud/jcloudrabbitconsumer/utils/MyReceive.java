package com.cloud.jcloudrabbitconsumer.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MyReceive implements ChannelAwareMessageListener/*, ChannelAwareBatchMessageListener*/ {

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deLiverTag = message.getMessageProperties().getDeliveryTag();
        try {
            String msg = message.toString();
            String[] msgArray = msg.split("'");//可
            Map<String, String> recv = objectMapper.readValue(msgArray[1].trim(), HashMap.class);
            // 以点进Message里面看源码,单引号直接的数据就是我们的map消息数据
//            Map<String, String> recv = mapStringToMap(msgArray[1].trim(),3);
            if ("TestDirectQueue".equals(message.getMessageProperties().getConsumerQueue())) {
                System.out.println("MyReceive" + recv.toString());
            }
            channel.basicAck(deLiverTag, true);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(deLiverTag, false);
        }
    }

//    @Override
//    public void onMessageBatch(List<Message> messages, Channel channel) {
//
//
//    }
}
