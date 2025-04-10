package org.site.survey.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private HttpStatus status;
    private String errorCode;
    private String message;
    private String path;
} 