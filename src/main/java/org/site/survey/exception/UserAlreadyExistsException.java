package org.site.survey.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserAlreadyExistsException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public UserAlreadyExistsException(String email) {
        super(String.format("User with either email or username '%s' already exists", email));
        this.status = HttpStatus.CONFLICT;
        this.errorCode = "USER_ALREADY_EXISTS";
    }
} 