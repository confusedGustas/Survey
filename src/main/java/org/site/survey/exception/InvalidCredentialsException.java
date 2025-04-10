package org.site.survey.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidCredentialsException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public InvalidCredentialsException() {
        super("Invalid username or password");
        this.status = HttpStatus.UNAUTHORIZED;
        this.errorCode = "INVALID_CREDENTIALS";
    }
} 