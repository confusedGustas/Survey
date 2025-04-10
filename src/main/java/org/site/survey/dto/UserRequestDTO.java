package org.site.survey.dto;

import lombok.Data;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
public class UserRequestDTO {
    @Schema(description = "Username of the user", example = "test")
    private String username;
    
    @Schema(description = "Password of the user", example = "Test123!")
    private String password;
    
    @Schema(description = "Email of the user", example = "test@gmail.com")
    private String email;
} 