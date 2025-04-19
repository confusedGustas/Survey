package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class ElasticsearchConnectionException extends BaseException {
    private final String message;
    
    public ElasticsearchConnectionException(String message) {
        this.message = message;
    }
    
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }

    @Override
    public String getErrorCode() {
        return "ELASTICSEARCH_CONNECTION_ERROR";
    }

    @Override
    public String getMessage() {
        return message != null ? message : "Elasticsearch connection failed";
    }
} 