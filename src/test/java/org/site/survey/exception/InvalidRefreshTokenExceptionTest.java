package org.site.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidRefreshTokenExceptionTest {

    @Test
    void getStatus_ShouldReturnUnauthorized() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException();
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void getErrorCode_ShouldReturnInvalidRefreshToken() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException();
        assertEquals("INVALID_REFRESH_TOKEN", exception.getErrorCode());
    }

    @Test
    void getMessage_ShouldReturnErrorMessage() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException();
        assertEquals("Invalid or expired refresh token", exception.getMessage());
    }
} 