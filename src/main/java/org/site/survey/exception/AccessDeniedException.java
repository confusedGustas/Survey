package org.site.survey.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AccessDeniedException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public AccessDeniedException() {
        super("Access denied");
        this.status = HttpStatus.FORBIDDEN;
        this.errorCode = "ACCESS_DENIED";
    }
} 