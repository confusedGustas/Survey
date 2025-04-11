package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class ServiceException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getErrorCode() {
        return "SERVICE_ERROR";
    }

    @Override
    public String getMessage() {
        return "An error occurred while processing the service request";
    }
} 