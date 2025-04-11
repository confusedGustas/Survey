package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "BAD_REQUEST";
    }

    @Override
    public String getMessage() {
        return "Invalid request parameters";
    }
} 