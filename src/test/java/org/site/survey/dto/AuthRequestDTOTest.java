package org.site.survey.dto;

import org.junit.jupiter.api.Test;
import org.site.survey.dto.request.AuthRequestDTO;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestDTOTest {

    @Test
    void setters_UpdateFieldsCorrectly() {
        AuthRequestDTO dto = new AuthRequestDTO();

        dto.setUsername("testuser");
        dto.setPassword("Password123!");

        assertEquals("testuser", dto.getUsername());
        assertEquals("Password123!", dto.getPassword());
    }
    
    @Test
    void equals_SameData_ReturnsTrue() {
        AuthRequestDTO dto1 = new AuthRequestDTO();
        dto1.setUsername("testuser");
        dto1.setPassword("Password123!");
                
        AuthRequestDTO dto2 = new AuthRequestDTO();
        dto2.setUsername("testuser");
        dto2.setPassword("Password123!");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    
    @Test
    void equals_DifferentData_ReturnsFalse() {
        AuthRequestDTO dto1 = new AuthRequestDTO();
        dto1.setUsername("testuser1");
        dto1.setPassword("Password1!");
                
        AuthRequestDTO dto2 = new AuthRequestDTO();
        dto2.setUsername("testuser2");
        dto2.setPassword("Password2!");

        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void toString_ContainsAllFields() {
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setUsername("testuser");
        dto.setPassword("Password123!");

        String dtoString = dto.toString();

        assertTrue(dtoString.contains("username=testuser"));
        assertTrue(dtoString.contains("password=Password123!"));
    }
} 