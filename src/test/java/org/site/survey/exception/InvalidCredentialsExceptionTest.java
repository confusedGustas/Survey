package org.site.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidCredentialsExceptionTest {

    @Test
    void getStatus_ShouldReturnUnauthorized() {
        InvalidCredentialsException exception = new InvalidCredentialsException();
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void getErrorCode_ShouldReturnInvalidCredentials() {
        InvalidCredentialsException exception = new InvalidCredentialsException();
        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
    }

    @Test
    void getMessage_ShouldReturnErrorMessage() {
        InvalidCredentialsException exception = new InvalidCredentialsException();
        assertEquals("Invalid username or password", exception.getMessage());
    }
} 
