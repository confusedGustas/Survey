package org.site.survey.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.site.survey.dto.request.AuthRequestDTO;
import org.site.survey.dto.response.AuthResponseDTO;
import org.site.survey.exception.AuthenticationException;
import org.site.survey.exception.InvalidTokenException;
import org.site.survey.exception.handler.GlobalExceptionHandler;
import org.site.survey.model.User;
import org.site.survey.service.UserService;
import org.site.survey.service.jwt.JwtService;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

class AuthControllerTest {
    private WebTestClient webTestClient;
    private UserService userService;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        jwtService = Mockito.mock(JwtService.class);
        AuthController authController = new AuthController(userService, jwtService);
        
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
            
        webTestClient = WebTestClient
                .bindToController(authController)
                .controllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    void login_ValidCredentials_ReturnsTokens() {
        AuthRequestDTO authRequest = new AuthRequestDTO();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password");

        User user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role("USER")
                .build();

        when(userService.authenticateUser("testuser", "password"))
                .thenReturn(Mono.just(user));
        when(jwtService.generateToken("testuser", "USER"))
                .thenReturn("access-token");
        when(jwtService.generateRefreshToken("testuser", "USER"))
                .thenReturn("refresh-token");

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDTO.class)
                .value(response -> {
                    assert response.getAccessToken().equals("access-token");
                    assert response.getRefreshToken().equals("refresh-token");
                });
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() {
        AuthRequestDTO authRequest = new AuthRequestDTO();
        authRequest.setUsername("testuser");
        authRequest.setPassword("wrongpassword");

        when(userService.authenticateUser("testuser", "wrongpassword"))
                .thenReturn(Mono.error(new AuthenticationException()));

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_NonExistentUser_ReturnsUnauthorized() {
        AuthRequestDTO authRequest = new AuthRequestDTO();
        authRequest.setUsername("nonexistent");
        authRequest.setPassword("password");

        when(userService.authenticateUser("nonexistent", "password"))
                .thenReturn(Mono.error(new AuthenticationException()));

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void refreshToken_ValidToken_ReturnsNewTokens() {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", "new-access-token");
        tokens.put("refreshToken", "new-refresh-token");

        when(jwtService.refreshTokens("valid-refresh-token"))
                .thenReturn(Mono.just(tokens));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/refresh")
                        .queryParam("refreshToken", "valid-refresh-token")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDTO.class)
                .value(response -> {
                    assert response.getAccessToken().equals("new-access-token");
                    assert response.getRefreshToken().equals("new-refresh-token");
                });
    }

    @Test
    void refreshToken_InvalidToken_ReturnsUnauthorized() {
        when(jwtService.refreshTokens("invalid-refresh-token"))
                .thenReturn(Mono.error(new InvalidTokenException()));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/refresh")
                        .queryParam("refreshToken", "invalid-refresh-token")
                        .build())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void refreshToken_ExpiredToken_StillGeneratesNewTokens() {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", "new-access-token");
        tokens.put("refreshToken", "new-refresh-token");

        when(jwtService.refreshTokens("expired-refresh-token"))
                .thenReturn(Mono.just(tokens));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/refresh")
                        .queryParam("refreshToken", "expired-refresh-token")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDTO.class)
                .value(response -> {
                    assert response.getAccessToken().equals("new-access-token");
                    assert response.getRefreshToken().equals("new-refresh-token");
                });
    }
} 
