package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class AuthenticationException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getErrorCode() {
        return "AUTHENTICATION_ERROR";
    }

    @Override
    public String getMessage() {
        return "Authentication failed: Invalid credentials";
    }
} 