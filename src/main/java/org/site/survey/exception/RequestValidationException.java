package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class RequestValidationException extends BaseException {
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
        return "Request validation failed: Invalid input data";
    }
} 