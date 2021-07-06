package com.cloud.jcloudrabbitconsumer;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;

@EnableRabbit
@SpringBootApplication
@EnableBinding(Sink.class)
public class JCloudRabbitConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JCloudRabbitConsumerApplication.class, args);
    }

}
