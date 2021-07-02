package com.cloud.jcloudfeign.common;

import com.cloud.jcloudfeign.feignclient.Provide1Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Provide1ClientFallBack implements Provide1Client {
    private static Logger logger = LoggerFactory.getLogger(Provide1ClientFallBack.class);

    @Override
    public String sayhello() {
        logger.info("进入熔断");
        return "error";
    }
}
