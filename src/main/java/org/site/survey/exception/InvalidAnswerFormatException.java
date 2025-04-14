package org.site.survey.exception;

import org.site.survey.exception.model.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidAnswerFormatException extends BaseException {
    private final String message;
    
    public InvalidAnswerFormatException(String message) {
        this.message = message;
    }
    
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "INVALID_ANSWER_FORMAT";
    }

    @Override
    public String getMessage() {
        return message;
    }
} 