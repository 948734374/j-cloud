package com.example.jcloudgateway.conFig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RoutConfig {
    @Autowired
    urls us;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder){  //RouteLocator是路由定位器

        RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();
        for (urls.urlInfo s:us.getUrlinfos()) {
            routes.route( s.getServiceName()//路由ID
                    , r->r.path("/"+s.getPathUrl()+"/**/**").uri("lb://"+s.getServiceName()))  //第一个路径是断言，第二个是URI
                    .build();
        }
        return routes.build();
    }
}