package org.site.survey.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.site.survey.model.User;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@DataR2dbcTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        databaseClient.sql("DELETE FROM users").then().block();
    }

    @Test
    void save_ValidUser_SavesSuccessfully() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        Mono<User> result = userRepository.save(user);

        StepVerifier.create(result)
                .expectNextMatches(savedUser -> 
                    savedUser.getId() != null &&
                    savedUser.getUsername().equals("testuser") &&
                    savedUser.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void findById_ExistingUser_ReturnsUser() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user).block();

        assert savedUser != null;
        Mono<User> result = userRepository.findById(savedUser.getId());

        StepVerifier.create(result)
                .expectNextMatches(foundUser -> 
                    foundUser.getId().equals(savedUser.getId()) &&
                    foundUser.getUsername().equals("testuser"))
                .verifyComplete();
    }

    @Test
    void findById_NonExistingUser_ReturnsEmpty() {
        Mono<User> result = userRepository.findById(99999);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findByUsername_ExistingUsername_ReturnsUser() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user).block();

        Mono<User> result = userRepository.findByUsername("testuser");

        StepVerifier.create(result)
                .expectNextMatches(foundUser -> 
                    foundUser.getUsername().equals("testuser") &&
                    foundUser.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void findByUsername_NonExistingUsername_ReturnsEmpty() {
        Mono<User> result = userRepository.findByUsername("nonexistent");

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user).block();

        Mono<User> result = userRepository.findByEmail("test@example.com");

        StepVerifier.create(result)
                .expectNextMatches(foundUser -> 
                    foundUser.getUsername().equals("testuser") &&
                    foundUser.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        Mono<User> result = userRepository.findByEmail("nonexistent@example.com");

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void delete_ExistingUser_DeletesSuccessfully() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user).block();

        assert savedUser != null;
        Mono<Void> deleteResult = userRepository.delete(savedUser);
        
        StepVerifier.create(deleteResult)
                .verifyComplete();
                
        Mono<User> findResult = userRepository.findById(savedUser.getId());
        
        StepVerifier.create(findResult)
                .expectNextCount(0)
                .verifyComplete();
    }
} 