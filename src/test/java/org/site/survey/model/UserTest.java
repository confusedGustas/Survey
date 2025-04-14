package org.site.survey.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserBuilder() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role("ADMIN")
                .createdAt(now)
                .build();

        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("ADMIN", user.getRole());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void testUserDefaultRole() {
        User user = User.builder().build();
        assertEquals("USER", user.getRole());
    }

    @Test
    void testUserSettersAndGetters() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        user.setId(1);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole("ADMIN");
        user.setCreatedAt(now);

        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("ADMIN", user.getRole());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void testEquals() {
        User user1 = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .build();

        User user2 = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .build();

        User user3 = User.builder()
                .id(2)
                .username("otheruser")
                .email("other@example.com")
                .build();

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void testToString() {
        User user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .build();

        String toString = user.toString();
        
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("username=testuser"));
        assertTrue(toString.contains("email=test@example.com"));
    }

    @Test
    void testNoArgsConstructor() {
        User user = new User();
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertEquals("USER", user.getRole());
        assertNull(user.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User(1, "testuser", "test@example.com", "password123", "ADMIN", now);
        
        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("ADMIN", user.getRole());
        assertEquals(now, user.getCreatedAt());
    }
} 
