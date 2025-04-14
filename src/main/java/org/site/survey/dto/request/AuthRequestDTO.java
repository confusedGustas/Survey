package org.site.survey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuthRequestDTO {
    @Schema(description = "Username of the user", example = "test")
    private String username;
    
    @Schema(description = "Password of the user", example = "Test123!")
    private String password;
} 