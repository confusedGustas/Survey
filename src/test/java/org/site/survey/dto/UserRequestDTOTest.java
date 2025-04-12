package org.site.survey.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserRequestDTOTest {

    @Test
    void builder_ValidData_CreatesDTOCorrectly() {
        String username = "testuser";
        String email = "test@example.com";
        String password = "Password123!";

        UserRequestDTO dto = UserRequestDTO.builder()
                .username(username)
                .email(email)
                .password(password)
                .build();

        assertEquals(username, dto.getUsername());
        assertEquals(email, dto.getEmail());
        assertEquals(password, dto.getPassword());
    }
    
    @Test
    void setters_UpdateFieldsCorrectly() {
        UserRequestDTO dto = new UserRequestDTO("testuser", "Password123!", "test@example.com");

        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("Password123!", dto.getPassword());
    }
    
    @Test
    void equals_SameData_ReturnsTrue() {
        UserRequestDTO dto1 = UserRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .build();
                
        UserRequestDTO dto2 = UserRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    
    @Test
    void equals_DifferentData_ReturnsFalse() {
        UserRequestDTO dto1 = UserRequestDTO.builder()
                .username("testuser1")
                .email("test1@example.com")
                .password("Password123!")
                .build();
                
        UserRequestDTO dto2 = UserRequestDTO.builder()
                .username("testuser2")
                .email("test2@example.com")
                .password("Password123!")
                .build();

        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void toString_ContainsAllFields() {
        UserRequestDTO dto = UserRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .build();

        String dtoString = dto.toString();

        assertTrue(dtoString.contains("username=testuser"));
        assertTrue(dtoString.contains("email=test@example.com"));
        assertTrue(dtoString.contains("password=Password123!"));
    }
} 