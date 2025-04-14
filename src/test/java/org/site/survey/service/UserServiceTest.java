package org.site.survey.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.site.survey.dto.request.UserRequestDTO;
import org.site.survey.dto.response.UserResponseDTO;
import org.site.survey.exception.UnauthorizedUserModificationException;
import org.site.survey.exception.UserAlreadyExistsException;
import org.site.survey.exception.UserNotFoundException;
import org.site.survey.integrity.UserDataIntegrity;
import org.site.survey.mapper.UserMapper;
import org.site.survey.model.User;
import org.site.survey.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.site.survey.type.RoleType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDataIntegrity userDataIntegrity;
    
    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UserService userService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, userDataIntegrity, userMapper);

        try {
            Field field = UserService.class.getDeclaredField("passwordEncoder");
            field.setAccessible(true);
            field.set(userService, passwordEncoder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mocked password encoder", e);
        }
        
        when(userMapper.mapToUserResponse(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return UserResponseDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void getAllUsers_Success() {
        User user1 = User.builder()
                .id(1)
                .username("user1")
                .email("user1@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();
                
        User user2 = User.builder()
                .id(2)
                .username("user2")
                .email("user2@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2));

        Flux<UserResponseDTO> result = userService.getAllUsers();

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getUsername().equals("user1"))
                .expectNextMatches(dto -> dto.getUsername().equals("user2"))
                .verifyComplete();
    }

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        User user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1)).thenReturn(Mono.just(user));

        Mono<UserResponseDTO> result = userService.getUserById(1);

        StepVerifier.create(result)
                .expectNextMatches(dto -> 
                    dto.getUsername().equals("testuser") && 
                    dto.getEmail().equals("test@example.com"))
                .verifyComplete();
        
        verify(userDataIntegrity).validateUserId(1);
    }

    @Test
    void getUserById_NonExistingUser_ThrowsException() {
        when(userRepository.findById(99)).thenReturn(Mono.empty());

        Mono<UserResponseDTO> result = userService.getUserById(99);

        StepVerifier.create(result)
                .expectError(UserNotFoundException.class)
                .verify();
        
        verify(userDataIntegrity).validateUserId(99);
    }

    @Test
    void getUserByUsername_ExistingUser_ReturnsUser() {
        User user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(user));

        Mono<UserResponseDTO> result = userService.getUserByUsername("testuser");

        StepVerifier.create(result)
                .expectNextMatches(dto -> 
                    dto.getUsername().equals("testuser") && 
                    dto.getEmail().equals("test@example.com"))
                .verifyComplete();
        
        verify(userDataIntegrity).validateUsername("testuser");
    }

    @Test
    void getUserByUsername_NonExistingUser_ThrowsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Mono.empty());

        Mono<UserResponseDTO> result = userService.getUserByUsername("nonexistent");

        StepVerifier.create(result)
                .expectError(UserNotFoundException.class)
                .verify();
        
        verify(userDataIntegrity).validateUsername("nonexistent");
    }

    @Test
    void createUser_Success() {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .build();
                
        User savedUser = User.builder()
                .id(1)
                .username("newuser")
                .email("new@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("newuser")).thenReturn(Mono.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Mono.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        Mono<UserResponseDTO> result = userService.createUser(requestDTO);

        StepVerifier.create(result)
                .expectNextMatches(dto -> 
                    dto.getUsername().equals("newuser") && 
                    dto.getEmail().equals("new@example.com"))
                .verifyComplete();
        
        verify(userDataIntegrity).validateUserRequest(requestDTO);
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("existinguser")
                .email("new@example.com")
                .password("password123")
                .build();
                
        User existingUser = User.builder()
                .id(1)
                .username("existinguser")
                .email("existing@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("existinguser")).thenReturn(Mono.just(existingUser));

        Mono<UserResponseDTO> result = userService.createUser(requestDTO);

        StepVerifier.create(result)
                .expectError(UserAlreadyExistsException.class)
                .verify();
        
        verify(userDataIntegrity).validateUserRequest(requestDTO);
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("newuser")
                .email("existing@example.com")
                .password("password123")
                .build();
                
        User existingUser = User.builder()
                .id(1)
                .username("existinguser")
                .email("existing@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("newuser")).thenReturn(Mono.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Mono.just(existingUser));

        Mono<UserResponseDTO> result = userService.createUser(requestDTO);

        StepVerifier.create(result)
                .expectError(UserAlreadyExistsException.class)
                .verify();
        
        verify(userDataIntegrity).validateUserRequest(requestDTO);
    }

    @Test
    void updateUser_Success() {
        String username = "existinguser";
        String currentUsername = "existinguser";
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("newpassword")
                .build();
                
        User existingUser = User.builder()
                .id(1)
                .username(username)
                .email("old@example.com")
                .password("oldencoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();
                
        User updatedUser = User.builder()
                .id(1)
                .username("updateduser")
                .email("updated@example.com")
                .password("newencoded")
                .role("USER")
                .createdAt(existingUser.getCreatedAt())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Mono.just(existingUser));
        when(userRepository.findByUsername("updateduser")).thenReturn(Mono.empty());
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Mono.empty());
        when(passwordEncoder.encode("newpassword")).thenReturn("newencoded");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        Mono<UserResponseDTO> result = userService.updateUser(username, requestDTO, currentUsername);

        StepVerifier.create(result)
                .expectNextMatches(dto -> 
                    dto.getUsername().equals("updateduser") && 
                    dto.getEmail().equals("updated@example.com"))
                .verifyComplete();
        
        verify(userDataIntegrity).validateUsername(username);
        verify(userDataIntegrity).validateUserRequest(requestDTO);
    }

    @Test
    void updateUser_DifferentUser_ThrowsException() {
        String username = "existinguser";
        String currentUsername = "differentuser";
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("newpassword")
                .build();

        Mono<UserResponseDTO> result = userService.updateUser(username, requestDTO, currentUsername);

        StepVerifier.create(result)
                .expectError(UnauthorizedUserModificationException.class)
                .verify();
        
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_Success() {
        String username = "existinguser";
        String currentUsername = "existinguser";
        
        User existingUser = User.builder()
                .id(1)
                .username(username)
                .email("user@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Mono.just(existingUser));
        when(userRepository.delete(existingUser)).thenReturn(Mono.empty());

        Mono<Object> result = userService.deleteUser(username, currentUsername);

        StepVerifier.create(result)
                .verifyComplete();
        
        verify(userDataIntegrity).validateUsername(username);
        verify(userRepository).delete(existingUser);
    }

    @Test
    void deleteUser_DifferentUser_ThrowsException() {
        String username = "existinguser";
        String currentUsername = "differentuser";

        Mono<Object> result = userService.deleteUser(username, currentUsername);

        StepVerifier.create(result)
                .expectError(UnauthorizedUserModificationException.class)
                .verify();
        
        verify(userRepository, never()).delete(any());
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsUser() {
        User user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);

        Mono<User> result = userService.authenticateUser("testuser", "password");

        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void authenticateUser_InvalidPassword_ThrowsException() {
        User user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("wrongpassword", "encoded")).thenReturn(false);

        Mono<User> result = userService.authenticateUser("testuser", "wrongpassword");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof Exception)
                .verify();
    }

    @Test
    void authenticateUser_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Mono.empty());

        Mono<User> result = userService.authenticateUser("nonexistent", "password");

        StepVerifier.create(result)
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    void updateUser_EmailAlreadyTakenByAnotherUser_ThrowsException() {
        User existingUser = User.builder()
                .id(1)
                .username("user1")
                .email("user1@example.com")
                .password("oldencoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();
                
        User anotherUser = User.builder()
                .id(2)
                .username("user2")
                .email("user2@example.com")
                .password("encoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();
                
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("updateduser1")
                .email("user2@example.com")
                .password("newpassword")
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Mono.just(existingUser));
        when(userRepository.findByUsername("updateduser1")).thenReturn(Mono.empty());
        when(userRepository.findByEmail("user2@example.com")).thenReturn(Mono.just(anotherUser));

        Mono<UserResponseDTO> result = userService.updateUser("user1", requestDTO, "user1");

        StepVerifier.create(result)
                .expectError(UserAlreadyExistsException.class)
                .verify();
        
        verify(userDataIntegrity).validateUsername("user1");
        verify(userDataIntegrity).validateUserRequest(requestDTO);
    }
    
    @Test
    void updateUser_SameUsernameButDifferentEmail_Success() {
        User existingUser = User.builder()
                .id(1)
                .username("user1")
                .email("old@example.com")
                .password("oldencoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();
                
        User updatedUser = User.builder()
                .id(1)
                .username("user1")
                .email("new@example.com")
                .password("newencoded")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();
                
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("user1")
                .email("new@example.com") 
                .password("newpassword")
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Mono.just(existingUser));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Mono.empty());
        when(passwordEncoder.encode("newpassword")).thenReturn("newencoded");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));
        when(userMapper.mapToUserResponse(updatedUser)).thenReturn(
            UserResponseDTO.builder()
                .id(1)
                .username("user1")
                .email("new@example.com")
                .role(RoleType.USER)
                .createdAt(updatedUser.getCreatedAt())
                .build()
        );

        Mono<UserResponseDTO> result = userService.updateUser("user1", requestDTO, "user1");

        StepVerifier.create(result)
                .expectNextMatches(dto -> 
                    dto.getUsername().equals("user1") && 
                    dto.getEmail().equals("new@example.com"))
                .verifyComplete();
        
        verify(userDataIntegrity).validateUsername("user1");
        verify(userDataIntegrity).validateUserRequest(requestDTO);
    }
} 
