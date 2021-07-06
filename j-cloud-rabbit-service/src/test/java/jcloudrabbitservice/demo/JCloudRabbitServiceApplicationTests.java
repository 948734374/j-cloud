package jcloudrabbitservice.demo;

import jcloudrabbitservice.demo.service.MqPushService;
import jcloudrabbitservice.demo.service.impl.MqPushServiceImpl1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@EnableBinding(Source.class)
@SpringBootTest
class JCloudRabbitServiceApplicationTests {
    @Autowired
    MqPushService mqPushService;
    @Autowired
    MqPushServiceImpl1 mqPushServiceImpl1;

    @Test
    void contextLoads() {
        mqPushService.send();
    }

    @Test
    void contextLoads1() {
        mqPushServiceImpl1.send1();
    }
}
