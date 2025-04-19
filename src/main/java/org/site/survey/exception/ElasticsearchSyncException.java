package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class ElasticsearchSyncException extends BaseException {
    private final String message;
    
    public ElasticsearchSyncException(String message) {
        this.message = message;
    }
    
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getErrorCode() {
        return "ELASTICSEARCH_SYNC_ERROR";
    }

    @Override
    public String getMessage() {
        return message != null ? message : "Elasticsearch synchronization failed";
    }
} 