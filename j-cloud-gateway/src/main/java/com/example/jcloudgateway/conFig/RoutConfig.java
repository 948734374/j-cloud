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
        for (String s:us.url) {
            routes.route( s //路由ID
                    , r->r.path("/**/**/**").uri("lb://"+s))  //第一个路径是断言，第二个是URI
                    .build();
        }
//        routes.route("j-cloud-provider1"  //路由ID
//                , r->r.path("/provider-user/**/**").uri("lb://provider-user"))  //第一个路径是断言，第二个是URI
//                .build();
         routes.build();
        return routes.build();
    }
}
