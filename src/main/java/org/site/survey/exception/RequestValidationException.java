package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class RequestValidationException extends BaseException {
    private final String message;
    
    public RequestValidationException() {
        this.message = "Request validation failed: Invalid input data";
    }
    
    public RequestValidationException(String message) {
        this.message = message;
    }
    
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "REQUEST_VALIDATION_ERROR";
    }

    @Override
    public String getMessage() {
        return this.message;
    }
} 