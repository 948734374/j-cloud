package com.example.jcloudgateway.conFig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
@ConfigurationProperties(prefix = "gateway.urls")
public class urls {
    String[] url;


    @Override
    public String toString() {
        return "urls{" +
                "url=" + Arrays.toString(url) +
                '}';
    }

    public String[] getUrl() {
        return url;
    }

    public void setUrl(String[] url) {
        this.url = url;
    }
}
