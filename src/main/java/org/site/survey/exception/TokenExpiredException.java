package org.site.survey.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TokenExpiredException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public TokenExpiredException() {
        super("Token has expired");
        this.status = HttpStatus.UNAUTHORIZED;
        this.errorCode = "TOKEN_EXPIRED";
    }
} 