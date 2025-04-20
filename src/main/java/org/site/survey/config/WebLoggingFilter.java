package org.site.survey.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@Order(1)
public class WebLoggingFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(WebLoggingFilter.class);

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String requestId = UUID.randomUUID().toString();

        String method = exchange.getRequest().getMethod().name();
        String uri = exchange.getRequest().getURI().toString();
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        Instant startTime = Instant.now();

        log.info("[{}] Request: {} {} from {}", requestId, method, uri, remoteAddress);

        if (log.isDebugEnabled()) {
            exchange.getRequest().getHeaders().forEach((name, values) ->
                    log.debug("[{}] Header: {}: {}", requestId, name, values));
        }

        exchange.getAttributes().put("requestId", requestId);
        exchange.getAttributes().put("startTime", startTime);

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> logResponse(exchange, startTime, requestId, null))
                .doOnError(throwable -> logResponse(exchange, startTime, requestId, throwable));
    }
    
    private void logResponse(ServerWebExchange exchange, Instant startTime, String requestId, Throwable throwable) {
        Duration duration = Duration.between(startTime, Instant.now());
        int statusCode = exchange.getResponse().getStatusCode() != null 
                ? exchange.getResponse().getStatusCode().value() 
                : 0;
        
        if (throwable != null) {
            log.error("[{}] Error Response: {} in {} ms - Exception: {}", 
                    requestId, statusCode, duration.toMillis(), throwable.getMessage(), throwable);
        } else {
            log.info("[{}] Response: {} in {} ms", 
                    requestId, statusCode, duration.toMillis());
            
            if (log.isDebugEnabled()) {
                exchange.getResponse().getHeaders().forEach((name, values) -> 
                    log.debug("[{}] Response Header: {}: {}", requestId, name, values));
            }
        }
    }
} 
