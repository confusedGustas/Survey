package org.site.survey.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.site.survey.exception.handler.GlobalExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.Map;
import java.util.Objects;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler exceptionHandler;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        exchange = MockServerWebExchange.from(request);
    }

    @Test
    void handleBaseException_ReturnsCorrectResponse() {
        AuthenticationException ex = new AuthenticationException();

        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleBaseException(ex, exchange);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assert responseEntity.getStatusCode() == HttpStatus.UNAUTHORIZED;
                    assert Objects.requireNonNull(responseEntity.getBody()).get("error").equals("Unauthorized");
                    assert responseEntity.getBody().get("path").equals("/api/test");
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_ReturnsInternalServerError() {
        Exception testException = new RuntimeException("Test exception");
        Mono<ResponseEntity<Map<String, Object>>> result = exceptionHandler.handleGenericException(testException, exchange);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assert responseEntity.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR;
                    assert Objects.requireNonNull(responseEntity.getBody()).get("error").equals("Internal Server Error");
                })
                .verifyComplete();
    }
} 
