package jcloudsonsumer.demo.common;

import org.springframework.messaging.Message;
public interface ReceiveMsg {


    public void receiveTime(Message<String> message);
}