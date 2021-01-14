package jcloudprovider1.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
//@EnableEurekaClient //表明这是一个eureka客户端
public class Provider1Application {

    public static void main(String[] args) {
        SpringApplication.run(Provider1Application.class, args);
    }

}
