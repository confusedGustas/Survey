package org.site.survey.exception.handler;

import org.site.survey.exception.AccessDeniedException;
import org.site.survey.exception.AuthenticationException;
import org.site.survey.exception.BadRequestException;
import org.site.survey.exception.ElasticsearchConnectionException;
import org.site.survey.exception.ElasticsearchException;
import org.site.survey.exception.ElasticsearchSyncException;
import org.site.survey.exception.InvalidAnswerFormatException;
import org.site.survey.exception.RequestValidationException;
import org.site.survey.exception.ResourceNotFoundException;
import org.site.survey.exception.ServerErrorException;
import org.site.survey.exception.SurveyNotFoundException;
import org.site.survey.exception.UnauthorizedSurveyAccessException;
import org.site.survey.exception.UnauthorizedUserModificationException;
import org.site.survey.exception.model.BaseException;
import org.site.survey.exception.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationExceptions(WebExchangeBindException ex, ServerWebExchange exchange) {
        log.error("Validation exception: ", ex);
        return buildErrorResponse(new RequestValidationException(), exchange);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResponseStatusException(
            ResponseStatusException ex, ServerWebExchange exchange) {
        
        log.error("Response status exception: ", ex);
        
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
        log.error("Base exception: ", ex);
        return buildErrorResponse(ex, exchange);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgumentException(IllegalArgumentException ex, ServerWebExchange exchange) {
        log.error("Illegal argument exception: ", ex);
        return buildErrorResponse(new BadRequestException(), exchange);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNoSuchElementException(NoSuchElementException ex, ServerWebExchange exchange) {
        log.error("No such element exception: ", ex);
        return buildErrorResponse(new ResourceNotFoundException(), exchange);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleSpringAccessDeniedException(AccessDeniedException ex, ServerWebExchange exchange) {
        log.error("Spring access denied exception: ", ex);
        return buildErrorResponse(new AccessDeniedException(), exchange);
    }
    
    @ExceptionHandler(UnauthorizedUserModificationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUnauthorizedUserModificationException(
            UnauthorizedUserModificationException ex, ServerWebExchange exchange) {
        log.error("Unauthorized user modification attempt: ", ex);
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("timestamp", LocalDateTime.now());
        errorMap.put("status", HttpStatus.FORBIDDEN.value());
        errorMap.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        errorMap.put("message", ex.getMessage());
        errorMap.put("path", exchange.getRequest().getPath().toString());
        return Mono.just(new ResponseEntity<>(errorMap, HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        log.error("Unhandled exception: ", ex);
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("timestamp", LocalDateTime.now());
        errorMap.put("status", 500);
        errorMap.put("error", "Internal Server Error");
        errorMap.put("message", ex.getMessage());
        errorMap.put("exception", ex.getClass().getCanonicalName());
        errorMap.put("path", exchange.getRequest().getPath().toString());
        return Mono.just(new ResponseEntity<>(errorMap, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(SurveyNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleSurveyNotFoundException(
            SurveyNotFoundException ex, ServerWebExchange exchange) {
        log.error("Survey not found exception: ", ex);
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("timestamp", LocalDateTime.now());
        errorMap.put("status", HttpStatus.NOT_FOUND.value());
        errorMap.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        errorMap.put("errorCode", ex.getErrorCode());
        errorMap.put("message", ex.getMessage());
        errorMap.put("path", exchange.getRequest().getPath().toString());
        return Mono.just(new ResponseEntity<>(errorMap, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(InvalidAnswerFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAnswerFormatException(InvalidAnswerFormatException ex) {
        log.error("InvalidAnswerFormatException: {}", ex.getMessage());
        return ErrorResponse.toResponseEntity(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedSurveyAccessException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUnauthorizedSurveyAccessException(
            UnauthorizedSurveyAccessException ex, ServerWebExchange exchange) {
        log.error("Unauthorized survey access exception: ", ex);
        return buildErrorResponse(ex, exchange);
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleRuntimeException(
            RuntimeException ex, ServerWebExchange exchange) {
        log.error("Runtime exception: ", ex);
        
        if (ex.getMessage() != null && ex.getMessage().equals("No response returned")) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("timestamp", LocalDateTime.now());
            errorMap.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorMap.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            errorMap.put("message", ex.getMessage());
            errorMap.put("path", exchange.getRequest().getPath().toString());
            return Mono.just(new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR));
        }
        
        return handleGenericException(ex, exchange);
    }

    @ExceptionHandler(ElasticsearchException.class)
    public ResponseEntity<ErrorResponse> handleElasticsearchException(ElasticsearchException ex) {
        log.error("ElasticsearchException: {}", ex.getMessage());
        return ErrorResponse.toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "ELASTICSEARCH_ERROR", ex.getMessage());
    }
    
    @ExceptionHandler(ElasticsearchConnectionException.class)
    public ResponseEntity<ErrorResponse> handleElasticsearchConnectionException(ElasticsearchConnectionException ex) {
        log.error("ElasticsearchConnectionException: {}", ex.getMessage());
        return ErrorResponse.toResponseEntity(HttpStatus.SERVICE_UNAVAILABLE, "ELASTICSEARCH_CONNECTION_ERROR", ex.getMessage());
    }
    
    @ExceptionHandler(ElasticsearchSyncException.class)
    public ResponseEntity<ErrorResponse> handleElasticsearchSyncException(ElasticsearchSyncException ex) {
        log.error("ElasticsearchSyncException: {}", ex.getMessage());
        return ErrorResponse.toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "ELASTICSEARCH_SYNC_ERROR", ex.getMessage());
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