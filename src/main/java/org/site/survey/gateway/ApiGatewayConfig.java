package org.site.survey.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("survey-service", r -> r.path("/api/surveys/**").uri("http://localhost:8080"))
                .route("user-service", r -> r.path("/api/users/**").uri("http://localhost:8080"))
                .route("admin-service", r -> r.path("/api/admin/**").uri("http://localhost:8080"))
                .build();
    }
}