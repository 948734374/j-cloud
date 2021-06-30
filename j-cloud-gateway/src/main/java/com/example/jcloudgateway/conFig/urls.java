package com.example.jcloudgateway.conFig;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@ConfigurationProperties(prefix = "urls")
public class urls {

    public List<urlInfo> urlinfos;

    public List<urlInfo> getUrlinfos() {
        return urlinfos;
    }

    public void setUrlinfos(List<urlInfo> urlinfos) {
        this.urlinfos = urlinfos;
    }
    public static class urlInfo {
        private String serviceName;
        private String pathUrl;

        public String getPathUrl() {
            return pathUrl;
        }

        public void setPathUrl(String pathUrl) {
            this.pathUrl = pathUrl;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
    }

}
