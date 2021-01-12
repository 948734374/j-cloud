package jcloudzookeeperclient.jcloudzookeeperclient.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class test {

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping(value = "/test/test1")
    public String paymentzk(){

        //指出服务地址   http://{服务提供者应用名名称}/{具体的controller}
        String url="http://cloud-provider/payment/zookeeper";

        //返回值类型和我们的业务返回值一致
        return restTemplate.getForObject(url, String.class);
    }
}
