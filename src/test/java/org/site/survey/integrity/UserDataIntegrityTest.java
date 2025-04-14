package org.site.survey.integrity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.site.survey.dto.request.UserRequestDTO;
import org.site.survey.exception.RequestValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserDataIntegrityTest {

    private UserDataIntegrity userDataIntegrity;

    @BeforeEach
    void setUp() {
        userDataIntegrity = new UserDataIntegrity();
    }

    @Test
    void validateUserId_ValidId_DoesNotThrowException() {
        assertDoesNotThrow(() -> userDataIntegrity.validateUserId(1));
    }

    @Test
    void validateUserId_InvalidId_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUserId(null));
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUserId(0));
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUserId(-1));
    }

    @Test
    void validateUsername_ValidUsername_DoesNotThrowException() {
        assertDoesNotThrow(() -> userDataIntegrity.validateUsername("validUsername"));
        assertDoesNotThrow(() -> userDataIntegrity.validateUsername("valid_username"));
        assertDoesNotThrow(() -> userDataIntegrity.validateUsername("valid-username123"));
    }

    @Test
    void validateUsername_InvalidUsername_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUsername(null));
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUsername(""));
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUsername("   "));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUsername("ab"));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUsername("usernamethatiswaytoolong"));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUsername("user@name"));
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUsername("user name"));
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUsername("user#name"));
    }

    @Test
    void validateEmail_ValidEmail_DoesNotThrowException() {
        assertDoesNotThrow(() -> userDataIntegrity.validateEmail("user@example.com"));
        assertDoesNotThrow(() -> userDataIntegrity.validateEmail("user.name@example.com"));
        assertDoesNotThrow(() -> userDataIntegrity.validateEmail("user+tag@example.com"));
        assertDoesNotThrow(() -> userDataIntegrity.validateEmail("user-name@sub.example.com"));
    }

    @Test
    void validateEmail_InvalidEmail_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateEmail(null));
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateEmail(""));
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateEmail("   "));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateEmail("userexample.com"));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateEmail("user@"));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateEmail("@example.com"));
    }

    @Test
    void validatePassword_ValidPassword_DoesNotThrowException() {
        assertDoesNotThrow(() -> userDataIntegrity.validatePassword("Password1!"));
        assertDoesNotThrow(() -> userDataIntegrity.validatePassword("StrongP@ssw0rd"));
        assertDoesNotThrow(() -> userDataIntegrity.validatePassword("C0mplex_P@ssword"));
    }

    @Test
    void validatePassword_InvalidPassword_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validatePassword(null));
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validatePassword(""));
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validatePassword("   "));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validatePassword("Short1!"));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validatePassword("lowercase123!"));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validatePassword("PASSWORD1!"));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validatePassword("Password!"));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validatePassword("Password1"));
        
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validatePassword("Pass word1!"));
    }

    @Test
    void validateUserRequest_ValidRequest_DoesNotThrowException() {
        UserRequestDTO validRequest = UserRequestDTO.builder()
                .username("validuser")
                .email("valid@example.com")
                .password("Valid1Password!")
                .build();
        
        assertDoesNotThrow(() -> userDataIntegrity.validateUserRequest(validRequest));
    }

    @Test
    void validateUserRequest_InvalidRequest_ThrowsException() {
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUserRequest(null));
        
        UserRequestDTO invalidUsername = UserRequestDTO.builder()
                .username("")
                .email("valid@example.com")
                .password("Valid1Password!")
                .build();
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUserRequest(invalidUsername));
        
        UserRequestDTO invalidEmail = UserRequestDTO.builder()
                .username("validuser")
                .email("invalidemail")
                .password("Valid1Password!")
                .build();
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUserRequest(invalidEmail));
        
        UserRequestDTO invalidPassword = UserRequestDTO.builder()
                .username("validuser")
                .email("valid@example.com")
                .password("weak")
                .build();
        assertThrows(RequestValidationException.class, () -> userDataIntegrity.validateUserRequest(invalidPassword));
    }
} 
