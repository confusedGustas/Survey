package org.site.survey.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedUserModificationException extends RuntimeException {
    public UnauthorizedUserModificationException() {
        super("You are not authorized to modify this user");
    }
} 