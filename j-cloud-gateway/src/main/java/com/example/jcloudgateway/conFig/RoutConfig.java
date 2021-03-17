package com.example.jcloudgateway.conFig;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RoutConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder){  //RouteLocator是路由定位器

        RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();
        routes.route("j-cloud-provider1"  //路由ID
                , r->r.path("/provider-user/**/**").uri("lb://provider-user"))  //第一个路径是断言，第二个是URI
                .build();
        return routes.build();
    }
}
