package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getErrorCode() {
        return "INVALID_TOKEN";
    }

    @Override
    public String getMessage() {
        return "Invalid or expired token";
    }
} 