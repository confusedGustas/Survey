package org.site.survey.exception.model;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {
    
    protected BaseException() {
        super();
    }
    
    public abstract HttpStatus getStatus();
    public abstract String getErrorCode();
    public abstract String getMessage();
} 