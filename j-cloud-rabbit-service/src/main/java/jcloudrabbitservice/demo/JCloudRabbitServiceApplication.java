package jcloudrabbitservice.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;

@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding(Source.class)
public class JCloudRabbitServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JCloudRabbitServiceApplication.class, args);
    }

}
