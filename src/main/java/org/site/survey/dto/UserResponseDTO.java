package org.site.survey.dto;

import lombok.Builder;
import lombok.Data;
import org.site.survey.type.Role;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDTO {
    private Integer id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
} 