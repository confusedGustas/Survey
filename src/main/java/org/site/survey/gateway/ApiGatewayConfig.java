package org.site.survey.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class ApiGatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // API routes
                .route("survey-service", r -> r.path("/api/surveys/**")
                        .uri("forward:/api/surveys"))
                .route("user-service", r -> r.path("/api/users/**")
                        .uri("forward:/api/users"))
                .route("admin-service", r -> r.path("/api/admin/**")
                        .uri("forward:/api/admin"))
                .build();
    }

    @Bean
    public GlobalFilter swaggerBypassFilter() {
        return new SwaggerBypassFilter();
    }

    private static class SwaggerBypassFilter implements GlobalFilter, Ordered {
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            String path = exchange.getRequest().getURI().getPath();

            // Bypass gateway for Swagger paths
            if (path.startsWith("/swagger-ui") ||
                    path.startsWith("/v3/api-docs") ||
                    path.startsWith("/webjars")) {
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}