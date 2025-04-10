package org.site.survey.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationExceptions(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return Mono.just(buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "REQUEST_VALIDATION_ERROR",
                message,
                exchange
        ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResponseStatusException(
            ResponseStatusException ex, ServerWebExchange exchange) {
        return Mono.just(buildErrorResponse(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason(),
                ex.getMessage(),
                exchange
        ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUserNotFoundException(UserNotFoundException ex, ServerWebExchange exchange) {
        return Mono.just(buildErrorResponse(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), exchange));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUserAlreadyExistsException(UserAlreadyExistsException ex, ServerWebExchange exchange) {
        return Mono.just(buildErrorResponse(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), exchange));
    }

    @ExceptionHandler(RequestValidationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleRequestValidationException(RequestValidationException ex, ServerWebExchange exchange) {
        return Mono.just(buildErrorResponse(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), exchange));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorMap(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred",
                        exchange
                )));
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String errorCode, String message, ServerWebExchange exchange) {
        return new ResponseEntity<>(buildErrorMap(status, errorCode, message, exchange), status);
    }

    private Map<String, Object> buildErrorMap(
            HttpStatus status, String errorCode, String message, ServerWebExchange exchange) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("timestamp", LocalDateTime.now());
        errorMap.put("status", status.value());
        errorMap.put("error", status.getReasonPhrase());
        errorMap.put("errorCode", errorCode);
        errorMap.put("message", message);
        errorMap.put("path", exchange.getRequest().getPath().toString());
        return errorMap;
    }
} 