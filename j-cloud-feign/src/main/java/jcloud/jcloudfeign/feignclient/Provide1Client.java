package jcloud.jcloudfeign.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

@Component
@FeignClient(value = "provider-user")
public interface Provide1Client {
    @PostMapping("/user/sayHello")
    public String sayhello();
}
