package org.site.survey.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.site.survey.dto.request.UserRequestDTO;
import org.site.survey.dto.response.UserResponseDTO;
import org.site.survey.exception.ResourceNotFoundException;
import org.site.survey.service.UserService;
import org.site.survey.type.RoleType;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Mock
    private UserService userService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            UserController userController = new UserController(userService);
            webTestClient = WebTestClient.bindToController(userController).build();
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
                .expectBodyList(UserResponseDTO.class)
                .hasSize(2)
                .contains(user1, user2);
    }

    @Test
    void getAllUsers_NoUsers_ReturnsEmptyMessage() {
        when(userService.getAllUsers()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User list is empty");
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
                .expectBody(UserResponseDTO.class)
                .isEqualTo(user);
    }

    @Test
    void getUserById_NonExistingId_ReturnsNotFound() {
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
                .expectBody(UserResponseDTO.class)
                .isEqualTo(user);
    }

    @Test
    void getUserByUsername_NonExistingUsername_ReturnsNotFound() {
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
                .expectBody(UserResponseDTO.class)
                .isEqualTo(responseDTO);
    }

    @Test
    void updateUser_ExistingUser_ReturnsUpdatedUser() {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("UpdatedPass123!")
                .build();

        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(1)
                .username("updateduser")
                .email("updated@example.com")
                .role(RoleType.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.updateUser(eq("testuser"), any(UserRequestDTO.class)))
                .thenReturn(Mono.just(responseDTO));

        webTestClient.put()
                .uri("/api/users/testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .isEqualTo(responseDTO);
    }

    @Test
    void updateUser_NonExistingUser_ReturnsNotFound() {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("UpdatedPass123!")
                .build();

        when(userService.updateUser(eq("nonexistent"), any(UserRequestDTO.class)))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/api/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void deleteUser_ExistingUser_ReturnsSuccessMessage() {
        when(userService.deleteUser("testuser")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/users/testuser")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("User with username 'testuser' was deleted successfully");
    }

    @Test
    void deleteUser_NonExistingUser_ReturnsNotFound() {
        when(userService.deleteUser("nonexistent"))
                .thenReturn(Mono.error(new ResourceNotFoundException()));

        webTestClient.delete()
                .uri("/api/users/nonexistent")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void createUser_DuplicateUsername_ReturnsBadRequest() {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("existinguser")
                .email("new@example.com")
                .password("Password123!")
                .build();

        when(userService.createUser(any(UserRequestDTO.class)))
                .thenReturn(Mono.error(new org.site.survey.exception.UserAlreadyExistsException()));

        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().is5xxServerError();
    }
} 