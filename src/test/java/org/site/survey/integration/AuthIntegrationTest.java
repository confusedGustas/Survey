package org.site.survey.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.site.survey.controller.AuthController;
import org.site.survey.dto.request.AuthRequestDTO;
import org.site.survey.dto.response.AuthResponseDTO;
import org.site.survey.exception.AuthenticationException;
import org.site.survey.exception.InvalidTokenException;
import org.site.survey.model.User;
import org.site.survey.service.UserService;
import org.site.survey.service.jwt.JwtService;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

class AuthIntegrationTest {
    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            AuthController authController = new AuthController(userService, jwtService);
            webTestClient = WebTestClient.bindToController(authController).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mocks", e);
        }
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
    void login_InvalidCredentials_ReturnsError() {
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
                .expectStatus().is5xxServerError();
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
    void refreshToken_InvalidToken_ReturnsError() {
        when(jwtService.refreshTokens("invalid-refresh-token"))
                .thenReturn(Mono.error(new InvalidTokenException()));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/refresh")
                        .queryParam("refreshToken", "invalid-refresh-token")
                        .build())
                .exchange()
                .expectStatus().is5xxServerError();
    }
} 
