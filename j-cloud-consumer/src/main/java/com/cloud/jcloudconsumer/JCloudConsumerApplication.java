package com.cloud.jcloudconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@ComponentScan({"com.cloud"})
public class JCloudConsumerApplication {

    public static void main(String[] args) {

        SpringApplication.run(JCloudConsumerApplication.class, args);

//        SpringApplication application = new SpringApplication(JCloudConsumerApplication.class);
//        application.setBannerMode(Banner.Mode.OFF);
//        application.run(args);
    }

}
