package org.site.survey.dto;

import org.junit.jupiter.api.Test;
import org.site.survey.type.RoleType;
import org.site.survey.dto.response.UserResponseDTO;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserResponseDTOTest {

    @Test
    void testGettersAndSetters() {
        LocalDateTime now = LocalDateTime.now();
        RoleType role = RoleType.USER;
        
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1);
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setRole(role);
        dto.setCreatedAt(now);
        
        assertEquals(1, dto.getId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals(role, dto.getRole());
        assertEquals(now, dto.getCreatedAt());
    }
    
    @Test
    void testConstructor() {
        LocalDateTime now = LocalDateTime.now();
        
        UserResponseDTO dto = new UserResponseDTO(1, "testuser", "test@example.com", RoleType.ADMIN, now);
        
        assertEquals(1, dto.getId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals(RoleType.ADMIN, dto.getRole());
        assertEquals(now, dto.getCreatedAt());
    }
    
    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        
        UserResponseDTO dto = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .role(RoleType.USER)
                .createdAt(now)
                .build();
        
        assertEquals(1, dto.getId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals(RoleType.USER, dto.getRole());
        assertEquals(now, dto.getCreatedAt());
    }
    
    @Test
    void testEquals() {
        UserResponseDTO dto1 = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .role(RoleType.ADMIN)
                .createdAt(LocalDateTime.of(2023, 1, 1, 0, 0))
                .build();
        
        UserResponseDTO dto2 = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .role(RoleType.USER)
                .createdAt(LocalDateTime.of(2023, 1, 1, 0, 0))
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
                .role(RoleType.USER)
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