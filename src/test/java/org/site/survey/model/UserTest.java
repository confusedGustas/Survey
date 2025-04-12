package org.site.survey.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    
    @Test
    void builder_ValidData_CreatesUserCorrectly() {
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String role = "ADMIN";
        LocalDateTime createdAt = LocalDateTime.now();

        User user = User.builder()
                .id(1)
                .username(username)
                .email(email)
                .password(password)
                .role(role)
                .createdAt(createdAt)
                .build();

        assertEquals(1, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
        assertEquals(createdAt, user.getCreatedAt());
    }
    
    @Test
    void defaultRole_WhenNotSpecified_IsUser() {
        String username = "testuser";

        User user = User.builder()
                .username(username)
                .build();

        assertEquals("USER", user.getRole());
    }
    
    @Test
    void equals_SameData_ReturnsTrue() {
        User user1 = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role("USER")
                .createdAt(LocalDateTime.of(2023, 1, 1, 0, 0))
                .build();
                
        User user2 = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role("USER")
                .createdAt(LocalDateTime.of(2023, 1, 1, 0, 0))
                .build();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }
    
    @Test
    void equals_DifferentData_ReturnsFalse() {
        User user1 = User.builder()
                .id(1)
                .username("testuser1")
                .email("test1@example.com")
                .build();
                
        User user2 = User.builder()
                .id(2)
                .username("testuser2")
                .email("test2@example.com")
                .build();

        assertNotEquals(user1, user2);
    }
    
    @Test
    void toString_ContainsAllFields() {
        User user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role("USER")
                .createdAt(LocalDateTime.of(2023, 1, 1, 0, 0))
                .build();
        
        String userString = user.toString();
        
        assertTrue(userString.contains("id=1"));
        assertTrue(userString.contains("username=testuser"));
        assertTrue(userString.contains("email=test@example.com"));
        assertTrue(userString.contains("password=password123"));
        assertTrue(userString.contains("role=USER"));
        assertTrue(userString.contains("createdAt=2023-01-01T00:00"));
    }
    
    @Test
    void noArgsConstructor_CreatesEmptyUser() {
        User user = new User();
        
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertEquals("USER", user.getRole());
        assertNull(user.getCreatedAt());
    }
    
    @Test
    void setters_UpdateFieldsCorrectly() {
        User user = new User();
        
        user.setId(1);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole("ADMIN");
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        
        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("ADMIN", user.getRole());
        assertEquals(now, user.getCreatedAt());
    }
} 