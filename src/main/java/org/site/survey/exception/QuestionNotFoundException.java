package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class QuestionNotFoundException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "QUESTION_NOT_FOUND";
    }

    @Override
    public String getMessage() {
        return "The requested question was not found";
    }
}