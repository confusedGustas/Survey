package org.site.survey.exception.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;

@Setter
@Getter
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String errorCode;
    private String message;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public static ResponseEntity<ErrorResponse> toResponseEntity(HttpStatus status, String errorCode, String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setErrorCode(errorCode);
        errorResponse.setMessage(message);
        return new ResponseEntity<>(errorResponse, status);
    }

}