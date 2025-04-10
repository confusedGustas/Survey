package org.site.survey.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public UserNotFoundException(String userId) {
        super(String.format("User with id '%s' not found", userId));
        this.status = HttpStatus.NOT_FOUND;
        this.errorCode = "USER_NOT_FOUND";
    }
} 