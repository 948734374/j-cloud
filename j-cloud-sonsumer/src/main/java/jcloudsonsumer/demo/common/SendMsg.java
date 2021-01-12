package jcloudsonsumer.demo.common;

import org.springframework.integration.core.MessageSource;

public interface SendMsg {
    public MessageSource<String> sendTime();
}
