package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class ChoiceNotFoundException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "CHOICE_NOT_FOUND";
    }

    @Override
    public String getMessage() {
        return "The requested choice was not found";
    }
} 