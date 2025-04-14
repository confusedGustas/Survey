package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class UnauthorizedSurveyAccessException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }

    @Override
    public String getErrorCode() {
        return "UNAUTHORIZED_SURVEY_ACCESS";
    }

    @Override
    public String getMessage() {
        return "You are not authorized to access this survey";
    }
} 