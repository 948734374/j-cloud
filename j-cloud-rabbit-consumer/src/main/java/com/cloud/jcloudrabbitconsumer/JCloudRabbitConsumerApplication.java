package com.cloud.jcloudrabbitconsumer;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableRabbit
@SpringBootApplication
public class JCloudRabbitConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JCloudRabbitConsumerApplication.class, args);
    }

}
