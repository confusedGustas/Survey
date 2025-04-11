package org.site.survey.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthenticationException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public AuthenticationException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
        this.errorCode = "AUTHENTICATION_ERROR";
    }
} 