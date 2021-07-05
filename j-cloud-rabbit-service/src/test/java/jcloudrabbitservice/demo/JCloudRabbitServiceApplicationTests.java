package jcloudrabbitservice.demo;

import jcloudrabbitservice.demo.service.MqPushService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JCloudRabbitServiceApplicationTests {
    @Autowired
    MqPushService mqPushService;

    @Test
    void contextLoads() {
        mqPushService.send();
    }

}
