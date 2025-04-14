package org.site.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.site.survey.type.Role;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
} 