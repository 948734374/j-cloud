package jcloud.jcloudfeign.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "provider-user")
public interface provide1Client {
    @PostMapping("/user/sayHello")
    public String sayhello();
}
