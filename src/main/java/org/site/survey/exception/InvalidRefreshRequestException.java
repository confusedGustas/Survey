package org.site.survey.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidRefreshRequestException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public InvalidRefreshRequestException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "INVALID_REFRESH_REQUEST";
    }
} 