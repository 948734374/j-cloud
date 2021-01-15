package com.example.jcloudconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class JCloudConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JCloudConsumerApplication.class, args);
    }

}
