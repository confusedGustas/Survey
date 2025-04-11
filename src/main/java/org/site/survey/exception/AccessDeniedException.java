package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }

    @Override
    public String getErrorCode() {
        return "ACCESS_DENIED";
    }

    @Override
    public String getMessage() {
        return "Access denied: Insufficient permissions";
    }
} 