package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class SurveyNotFoundException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "SURVEY_NOT_FOUND";
    }

    @Override
    public String getMessage() {
        return "The requested survey was not found";
    }
}