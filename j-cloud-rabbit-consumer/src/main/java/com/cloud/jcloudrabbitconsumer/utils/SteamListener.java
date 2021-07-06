package com.cloud.jcloudrabbitconsumer.utils;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;


public class SteamListener {
    @StreamListener(Sink.INPUT)
//    @StreamListener
    public void receive(Object o){
        System.out.println("steam收到："+o.toString());
    }
}
