package org.site.survey.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.site.survey.controller.UserController;
import org.site.survey.dto.request.UserRequestDTO;
import org.site.survey.dto.response.UserResponseDTO;
import org.site.survey.exception.ResourceNotFoundException;
import org.site.survey.exception.UserAlreadyExistsException;
import org.site.survey.service.UserService;
import org.site.survey.type.RoleType;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class UserIntegrationTest {

    @Mock
    private UserService userService;

    private WebTestClient webTestClient;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            UserController userController = new UserController(userService);
            webTestClient = WebTestClient.bindToController(userController).build();
            
            when(authentication.getName()).thenReturn("testuser");
            when(securityContext.getAuthentication()).thenReturn(authentication);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mocks", e);
        }
    }

    @Test
    void getAllUsers_WithUsers_ReturnsUserList() {
        UserResponseDTO user1 = UserResponseDTO.builder()
                .id(1)
                .username("user1")
                .email("user1@example.com")
                .role(RoleType.USER)
                .createdAt(LocalDateTime.now())
                .build();

        UserResponseDTO user2 = UserResponseDTO.builder()
                .id(2)
                .username("user2")
                .email("user2@example.com")
                .role(RoleType.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getAllUsers()).thenReturn(Flux.just(user1, user2));

        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.length()").isEqualTo(2);
    }

    @Test
    void getAllUsers_NoUsers_ReturnsEmptyMessage() {
        when(userService.getAllUsers()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("No users found");
    }

    @Test
    void getUserById_ExistingId_ReturnsUser() {
        UserResponseDTO user = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .role(RoleType.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserById(1)).thenReturn(Mono.just(user));

        webTestClient.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.id").isEqualTo(1)
                .jsonPath("$.data.username").isEqualTo("testuser");
    }

    @Test
    void getUserById_NonExistingId_ReturnsError() {
        when(userService.getUserById(99)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/users/99")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getUserByUsername_ExistingUsername_ReturnsUser() {
        UserResponseDTO user = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .role(RoleType.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserByUsername("testuser")).thenReturn(Mono.just(user));

        webTestClient.get()
                .uri("/api/users/username/testuser")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.id").isEqualTo(1)
                .jsonPath("$.data.username").isEqualTo("testuser");
    }

    @Test
    void getUserByUsername_NonExistingUsername_ReturnsError() {
        when(userService.getUserByUsername("nonexistent")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/users/username/nonexistent")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void createUser_ValidInput_ReturnsCreatedUser() {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("Password123!")
                .build();

        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(3)
                .username("newuser")
                .email("newuser@example.com")
                .role(RoleType.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(Mono.just(responseDTO));

        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.id").isEqualTo(3)
                .jsonPath("$.data.username").isEqualTo("newuser");
    }

    @Test
    void createUser_DuplicateUsername_ReturnsError() {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("existinguser")
                .email("new@example.com")
                .password("Password123!")
                .build();

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(Mono.error(new UserAlreadyExistsException()));

        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void updateUser_NonExistingUser_ReturnsError() {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("UpdatedPass123!")
                .build();

        when(userService.updateUser(eq("nonexistent"), any(UserRequestDTO.class), eq("testuser"))).thenReturn(Mono.error(new ResourceNotFoundException()));

        StepVerifier.create(
            userService.updateUser("nonexistent", requestDTO, "testuser")
                .contextWrite(ctx -> ctx.put(ReactiveSecurityContextHolder.class.getName(), Mono.just(securityContext)))
        )
        .expectError(ResourceNotFoundException.class)
        .verify();
        
        webTestClient.put()
                .uri("/api/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void deleteUser_ExistingUser_ReturnsSuccessMessage() {
        UserResponseDTO user = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .role(RoleType.USER)
                .build();
                
        when(userService.getUserByUsername("testuser")).thenReturn(Mono.just(user));
        when(userService.deleteUser(eq("testuser"), eq("testuser"))).thenReturn(Mono.empty());

        StepVerifier.create(
            userService.deleteUser("testuser", "testuser")
                .contextWrite(ctx -> ctx.put(ReactiveSecurityContextHolder.class.getName(), Mono.just(securityContext)))
        )
        .verifyComplete();
        
        webTestClient.delete()
                .uri("/api/users/testuser")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.message").exists();
    }
} 
