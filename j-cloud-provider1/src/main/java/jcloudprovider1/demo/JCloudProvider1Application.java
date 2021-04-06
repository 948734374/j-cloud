package jcloudprovider1.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class JCloudProvider1Application {

    public static void main(String[] args) {
        SpringApplication.run(JCloudProvider1Application.class, args);
    }

}
