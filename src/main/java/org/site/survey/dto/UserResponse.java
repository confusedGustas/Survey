package org.site.survey.dto;

import lombok.Builder;
import lombok.Data;
import org.site.survey.model.Role;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Integer id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
} 