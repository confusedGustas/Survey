package org.site.survey.dto;

import org.junit.jupiter.api.Test;
import org.site.survey.type.Role;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserResponseDTOTest {

    @Test
    void builder_ValidData_CreatesDTOCorrectly() {
        Integer id = 1;
        String username = "testuser";
        String email = "test@example.com";
        Role role = Role.USER;
        LocalDateTime createdAt = LocalDateTime.now();

        UserResponseDTO dto = UserResponseDTO.builder()
                .id(id)
                .username(username)
                .email(email)
                .role(role)
                .createdAt(createdAt)
                .build();

        assertEquals(id, dto.getId());
        assertEquals(username, dto.getUsername());
        assertEquals(email, dto.getEmail());
        assertEquals(role, dto.getRole());
        assertEquals(createdAt, dto.getCreatedAt());
    }
    
    @Test
    void setters_UpdateFieldsCorrectly() {
        UserResponseDTO dto = new UserResponseDTO(1, "testuser", "test@example.com", Role.ADMIN, LocalDateTime.now());
        assertEquals(1, dto.getId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals(Role.ADMIN, dto.getRole());
    }
    
    @Test
    void equals_SameData_ReturnsTrue() {
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 0, 0);
        
        UserResponseDTO dto1 = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .createdAt(createdAt)
                .build();
                
        UserResponseDTO dto2 = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .createdAt(createdAt)
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    
    @Test
    void equals_DifferentData_ReturnsFalse() {
        UserResponseDTO dto1 = UserResponseDTO.builder()
                .id(1)
                .username("testuser1")
                .email("test1@example.com")
                .role(Role.USER)
                .build();
                
        UserResponseDTO dto2 = UserResponseDTO.builder()
                .id(2)
                .username("testuser2")
                .email("test2@example.com")
                .role(Role.ADMIN)
                .build();
        
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void toString_ContainsAllFields() {
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 0, 0);
        
        UserResponseDTO dto = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .createdAt(createdAt)
                .build();
        
        String dtoString = dto.toString();
        
        assertTrue(dtoString.contains("id=1"));
        assertTrue(dtoString.contains("username=testuser"));
        assertTrue(dtoString.contains("email=test@example.com"));
        assertTrue(dtoString.contains("role=USER"));
        assertTrue(dtoString.contains("createdAt=2023-01-01T00:00"));
    }
} 