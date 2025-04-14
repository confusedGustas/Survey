package org.site.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessDeniedExceptionTest {

    @Test
    void getStatus_ShouldReturnForbidden() {
        AccessDeniedException exception = new AccessDeniedException();
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getErrorCode_ShouldReturnAccessDenied() {
        AccessDeniedException exception = new AccessDeniedException();
        assertEquals("ACCESS_DENIED", exception.getErrorCode());
    }

    @Test
    void getMessage_ShouldReturnErrorMessage() {
        AccessDeniedException exception = new AccessDeniedException();
        assertEquals("Access denied: Insufficient permissions", exception.getMessage());
    }
} 
