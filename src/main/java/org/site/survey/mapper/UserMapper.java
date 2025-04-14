package org.site.survey.mapper;

import org.site.survey.dto.response.UserResponseDTO;
import org.site.survey.model.User;
import org.site.survey.type.RoleType;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO mapToUserResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(RoleType.valueOf(user.getRole()))
                .createdAt(user.getCreatedAt())
                .build();
    }
} 