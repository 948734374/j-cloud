package com.example.jcloudgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class JCloudGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(JCloudGatewayApplication.class, args);
    }

}
