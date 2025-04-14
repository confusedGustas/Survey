package org.site.survey.dto;

import org.junit.jupiter.api.Test;
import org.site.survey.dto.response.AuthResponseDTO;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseDTOTest {

    @Test
    void constructor_ValidData_CreatesDTOCorrectly() {
        String accessToken = "access-token-value";
        String refreshToken = "refresh-token-value";
        
        AuthResponseDTO dto = new AuthResponseDTO(accessToken, refreshToken);
        
        assertEquals(accessToken, dto.getAccessToken());
        assertEquals(refreshToken, dto.getRefreshToken());
    }
    
    @Test
    void setters_UpdateFieldsCorrectly() {
        AuthResponseDTO dto = new AuthResponseDTO("initial-access", "initial-refresh");
        
        dto.setAccessToken("new-access-token");
        dto.setRefreshToken("new-refresh-token");
        
        assertEquals("new-access-token", dto.getAccessToken());
        assertEquals("new-refresh-token", dto.getRefreshToken());
    }
    
    @Test
    void equals_SameData_ReturnsTrue() {
        AuthResponseDTO dto1 = new AuthResponseDTO("access-token", "refresh-token");
        AuthResponseDTO dto2 = new AuthResponseDTO("access-token", "refresh-token");
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    
    @Test
    void equals_DifferentData_ReturnsFalse() {
        AuthResponseDTO dto1 = new AuthResponseDTO("access-token-1", "refresh-token-1");
        AuthResponseDTO dto2 = new AuthResponseDTO("access-token-2", "refresh-token-2");
        
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void toString_ContainsAllFields() {
        AuthResponseDTO dto = new AuthResponseDTO("access-token", "refresh-token");
        
        String dtoString = dto.toString();
        
        assertTrue(dtoString.contains("accessToken=access-token"));
        assertTrue(dtoString.contains("refreshToken=refresh-token"));
    }
} 
