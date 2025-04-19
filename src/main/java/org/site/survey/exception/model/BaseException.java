package org.site.survey.exception.model;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {
    
    public BaseException() {
        super();
    }
    
    public abstract HttpStatus getStatus();
    
    public abstract String getErrorCode();
    
    @Override
    public abstract String getMessage();
} 