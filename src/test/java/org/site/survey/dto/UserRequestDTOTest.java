package org.site.survey.dto;

import org.junit.jupiter.api.Test;
import org.site.survey.dto.request.UserRequestDTO;

import static org.junit.jupiter.api.Assertions.*;

class UserRequestDTOTest {

    @Test
    void testBuilder() {
        UserRequestDTO dto = UserRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("password123", dto.getPassword());
    }
    
    @Test
    void testSettersAndGetters() {
        UserRequestDTO dto = UserRequestDTO.builder().build();
        
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("password123", dto.getPassword());
    }
    
    @Test
    void testEquals() {
        UserRequestDTO dto1 = UserRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        UserRequestDTO dto2 = UserRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        UserRequestDTO dto3 = UserRequestDTO.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password456")
                .build();
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
    
    @Test
    void testToString() {
        UserRequestDTO dto = UserRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        String toString = dto.toString();
        
        assertTrue(toString.contains("username=testuser"));
        assertTrue(toString.contains("email=test@example.com"));
        assertTrue(toString.contains("password=password123"));
    }
} 