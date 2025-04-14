package org.site.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidAnswerFormatExceptionTest {

    @Test
    void getStatus_ShouldReturnBadRequest() {
        InvalidAnswerFormatException exception = new InvalidAnswerFormatException("Test message");
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getErrorCode_ShouldReturnInvalidAnswerFormat() {
        InvalidAnswerFormatException exception = new InvalidAnswerFormatException("Test message");
        assertEquals("INVALID_ANSWER_FORMAT", exception.getErrorCode());
    }

    @Test
    void getMessage_ShouldReturnCustomMessage() {
        String message = "Custom error message";
        InvalidAnswerFormatException exception = new InvalidAnswerFormatException(message);
        assertEquals(message, exception.getMessage());
    }
} 