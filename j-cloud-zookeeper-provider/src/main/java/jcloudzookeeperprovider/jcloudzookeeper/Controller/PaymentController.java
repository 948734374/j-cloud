package jcloudzookeeperprovider.jcloudzookeeper.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
@RestController
public class PaymentController {
    @Value("${server.port}")
    private String serverPort;

    @RequestMapping(value = "/payment/zookeeper")
    public String paymentzk(){
        return "springcloud with zookeeper："+serverPort+"\t"+ UUID.randomUUID().toString();
    }


}
