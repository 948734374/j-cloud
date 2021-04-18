package jcloud.jcloudfeign.feignclient;

import jcloud.jcloudfeign.common.Provide1ClientFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

@Component
@FeignClient(value = "provider",fallback = Provide1ClientFallBack.class)
public interface Provide1Client {
    @PostMapping("/user/sayHello")
    public String sayhello();
}
