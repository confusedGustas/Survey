package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class QuestionHasAnswersException extends BaseException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "QUESTION_HAS_ANSWERS";
    }

    @Override
    public String getMessage() {
        return "Cannot delete question with existing answers";
    }
} 