package jcloudzookeeperclient.jcloudzookeeperclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class JCloudZookeeperClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(JCloudZookeeperClientApplication.class, args);
        System.out.println("<<<<<<<<<<<<<<<<<<<<请求客户端启动");
    }

}
