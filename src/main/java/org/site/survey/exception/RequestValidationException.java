package org.site.survey.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RequestValidationException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public RequestValidationException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "REQUEST_VALIDATION_ERROR";
    }
} 