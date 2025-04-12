package org.site.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserNotFoundExceptionTest {

    @Test
    void getStatus_ShouldReturnNotFound() {
        UserNotFoundException exception = new UserNotFoundException();
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getErrorCode_ShouldReturnUserNotFound() {
        UserNotFoundException exception = new UserNotFoundException();
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void getMessage_ShouldReturnErrorMessage() {
        UserNotFoundException exception = new UserNotFoundException();
        assertEquals("User not found", exception.getMessage());
    }
} 