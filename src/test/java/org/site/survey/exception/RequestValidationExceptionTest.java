package org.site.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestValidationExceptionTest {

    @Test
    void getStatus_ShouldReturnBadRequest() {
        RequestValidationException exception = new RequestValidationException();
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getErrorCode_ShouldReturnValidationError() {
        RequestValidationException exception = new RequestValidationException();
        assertEquals("REQUEST_VALIDATION_ERROR", exception.getErrorCode());
    }

    @Test
    void getMessage_ShouldReturnErrorMessage() {
        RequestValidationException exception = new RequestValidationException();
        assertEquals("Request validation failed: Invalid input data", exception.getMessage());
    }
} 
