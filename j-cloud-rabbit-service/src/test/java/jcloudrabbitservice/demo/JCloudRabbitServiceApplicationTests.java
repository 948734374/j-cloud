package jcloudrabbitservice.demo;

import jcloudrabbitservice.demo.service.MqPushService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;

@EnableBinding(Source.class)
@SpringBootTest
class JCloudRabbitServiceApplicationTests {
    @Autowired
    MqPushService mqPushService;

    @Test
    void contextLoads() {
        mqPushService.send();
    }

    @Test
    void contextLoads1() {
        mqPushService.send1();
    }
}
