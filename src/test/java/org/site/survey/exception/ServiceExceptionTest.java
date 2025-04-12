package org.site.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceExceptionTest {

    @Test
    void getStatus_ShouldReturnInternalServerError() {
        ServiceException exception = new ServiceException();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void getErrorCode_ShouldReturnServiceError() {
        ServiceException exception = new ServiceException();
        assertEquals("SERVICE_ERROR", exception.getErrorCode());
    }

    @Test
    void getMessage_ShouldReturnErrorMessage() {
        ServiceException exception = new ServiceException();
        assertEquals("An error occurred while processing the service request", exception.getMessage());
    }
} 