package com.example.jcloudgateway.conFig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GateWayConfig implements GlobalFilter , Ordered {
    private Logger logger =  LoggerFactory.getLogger(GateWayConfig.class);

  @Autowired
  urls urls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String host = exchange.getRequest().getURI().getHost();

        String[] url = urls.getUrl();

//        if(path!=null&& Arrays.asList(url).contains(path)){
//            白名单时使用
//            return chain.filter(exchange);
//        }

        logger.info("path:"+path);
        logger.info("host:"+host);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
