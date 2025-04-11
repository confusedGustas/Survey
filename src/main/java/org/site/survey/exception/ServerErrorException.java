package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class ServerErrorException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getErrorCode() {
        return "SERVER_ERROR";
    }

    @Override
    public String getMessage() {
        return "An unexpected server error occurred";
    }
} 