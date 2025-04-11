package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "USER_NOT_FOUND";
    }

    @Override
    public String getMessage() {
        return "User not found";
    }
} 