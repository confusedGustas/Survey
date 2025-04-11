package org.site.survey.exception.handler;

import org.site.survey.exception.AccessDeniedException;
import org.site.survey.exception.AuthenticationException;
import org.site.survey.exception.BadRequestException;
import org.site.survey.exception.RequestValidationException;
import org.site.survey.exception.ResourceNotFoundException;
import org.site.survey.exception.ServerErrorException;
import org.site.survey.exception.model.BaseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationExceptions(ServerWebExchange exchange) {
        return buildErrorResponse(new RequestValidationException(), exchange);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResponseStatusException(
            ResponseStatusException ex, ServerWebExchange exchange) {
        
        if (ex.getStatusCode().value() == 404) {
            return buildErrorResponse(new ResourceNotFoundException(), exchange);
        } else if (ex.getStatusCode().value() == 400) {
            return buildErrorResponse(new BadRequestException(), exchange);
        } else if (ex.getStatusCode().value() == 401) {
            return buildErrorResponse(new AuthenticationException(), exchange);
        } else if (ex.getStatusCode().value() == 403) {
            return buildErrorResponse(new AccessDeniedException(), exchange);
        } else {
            return buildErrorResponse(new ServerErrorException(), exchange);
        }
    }

    @ExceptionHandler(BaseException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleBaseException(BaseException ex, ServerWebExchange exchange) {
        return buildErrorResponse(ex, exchange);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgumentException(ServerWebExchange exchange) {
        return buildErrorResponse(new BadRequestException(), exchange);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNoSuchElementException(ServerWebExchange exchange) {
        return buildErrorResponse(new ResourceNotFoundException(), exchange);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleSpringAccessDeniedException(ServerWebExchange exchange) {
        return buildErrorResponse(new AccessDeniedException(), exchange);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(ServerWebExchange exchange) {
        return buildErrorResponse(new ServerErrorException(), exchange);
    }

    private Mono<ResponseEntity<Map<String, Object>>> buildErrorResponse(BaseException ex, ServerWebExchange exchange) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("timestamp", LocalDateTime.now());
        errorMap.put("status", ex.getStatus().value());
        errorMap.put("error", ex.getStatus().getReasonPhrase());
        errorMap.put("errorCode", ex.getErrorCode());
        errorMap.put("message", ex.getMessage());
        errorMap.put("path", exchange.getRequest().getPath().toString());
        return Mono.just(new ResponseEntity<>(errorMap, ex.getStatus()));
    }
} 