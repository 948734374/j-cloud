package jcloudzookeeperprovider.jcloudzookeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication

@EnableDiscoveryClient
public class JCloudZookeeperApplication {

    public static void main(String[] args) {
        SpringApplication.run(JCloudZookeeperApplication.class, args);
        System.out.println("<<<<<<<<<<<<<<<<<<<<服务端启动");
    }

}
