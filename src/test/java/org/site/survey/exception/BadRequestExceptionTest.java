package org.site.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BadRequestExceptionTest {

    @Test
    void getStatus_ShouldReturnBadRequest() {
        BadRequestException exception = new BadRequestException();
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getErrorCode_ShouldReturnBadRequest() {
        BadRequestException exception = new BadRequestException();
        assertEquals("BAD_REQUEST", exception.getErrorCode());
    }

    @Test
    void getMessage_ShouldReturnErrorMessage() {
        BadRequestException exception = new BadRequestException();
        assertEquals("Invalid request parameters", exception.getMessage());
    }
} 