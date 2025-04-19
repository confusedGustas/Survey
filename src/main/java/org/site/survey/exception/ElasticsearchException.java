package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class ElasticsearchException extends BaseException {
    private final String message;
    
    public ElasticsearchException(String message) {
        this.message = message;
    }
    
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "ELASTICSEARCH_ERROR";
    }

    @Override
    public String getMessage() {
        return message != null ? message : "Elasticsearch operation failed";
    }
} 