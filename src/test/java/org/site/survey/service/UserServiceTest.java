package org.site.survey.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.site.survey.exception.InvalidCredentialsException;
import org.site.survey.exception.UserNotFoundException;
import org.site.survey.integrity.UserDataIntegrity;
import org.site.survey.model.User;
import org.site.survey.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

class UserServiceTest {
    private UserService userService;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        UserDataIntegrity userDataIntegrity = Mockito.mock(UserDataIntegrity.class);
        passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        
        userService = new UserService(userRepository, userDataIntegrity);

        java.lang.reflect.Field field;
        try {
            field = UserService.class.getDeclaredField("passwordEncoder");
            field.setAccessible(true);
            field.set(userService, passwordEncoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsUser() {
        User user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role("USER")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        Mono<User> result = userService.authenticateUser("testuser", "password");

        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void authenticateUser_UserNotFound_ReturnsError() {
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Mono.empty());

        Mono<User> result = userService.authenticateUser("nonexistentuser", "password");

        StepVerifier.create(result)
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    void authenticateUser_InvalidPassword_ReturnsError() {
        User user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role("USER")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        Mono<User> result = userService.authenticateUser("testuser", "wrongpassword");

        StepVerifier.create(result)
                .expectError(InvalidCredentialsException.class)
                .verify();
    }
} 