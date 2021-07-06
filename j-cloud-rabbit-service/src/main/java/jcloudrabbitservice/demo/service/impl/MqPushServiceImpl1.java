package jcloudrabbitservice.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class MqPushServiceImpl1 {
    @Autowired
    Source source;


    public void send1(){
        Message<String> message = MessageBuilder.withPayload("1111").build();
        source.output().send(message);
    }
}
