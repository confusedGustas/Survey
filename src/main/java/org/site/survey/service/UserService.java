package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.site.survey.dto.UserRequestDTO;
import org.site.survey.dto.UserResponseDTO;
import org.site.survey.exception.UserNotFoundException;
import org.site.survey.exception.UserAlreadyExistsException;
import org.site.survey.exception.InvalidCredentialsException;
import org.site.survey.exception.RequestValidationException;
import org.site.survey.model.Role;
import org.site.survey.model.User;
import org.site.survey.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_])(?=\\S+$).{8,}$");

    public Flux<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponseDTO> getUserById(Integer id) {
        if (id == null || id <= 0) {
            return Mono.error(new RequestValidationException("Invalid user ID"));
        }
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(String.valueOf(id))))
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponseDTO> getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Mono.error(new RequestValidationException("Username cannot be empty"));
        }
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponseDTO> createUser(UserRequestDTO userRequestDTO) {
        return validateUserRequest(userRequestDTO)
                .flatMap(request -> userRepository.findByUsername(request.getUsername())
                        .hasElement()
                        .flatMap(usernameExists -> {
                            if (usernameExists) {
                                return Mono.error(new UserAlreadyExistsException(request.getUsername()));
                            }
                            return userRepository.findByEmail(request.getEmail())
                                    .hasElement()
                                    .flatMap(emailExists -> {
                                        if (emailExists) {
                                            return Mono.error(new UserAlreadyExistsException(request.getEmail()));
                                        }
                                        User user = User.builder()
                                                .username(request.getUsername())
                                                .email(request.getEmail())
                                                .password(passwordEncoder.encode(request.getPassword()))
                                                .role(String.valueOf(Role.USER))
                                                .createdAt(LocalDateTime.now())
                                                .build();
                                        
                                        return userRepository.save(user)
                                                .map(this::mapToUserResponse);
                                    });
                        }))
                .onErrorMap(throwable -> {
                    if (throwable instanceof UserAlreadyExistsException || 
                        throwable instanceof RequestValidationException) {
                        return throwable;
                    }
                    return new RuntimeException("Failed to create user: " + throwable.getMessage());
                });
    }

    public Mono<UserResponseDTO> updateUser(String username, UserRequestDTO userRequestDTO) {
        if (username == null || username.trim().isEmpty()) {
            return Mono.error(new RequestValidationException("Username cannot be empty"));
        }
        
        return validateUserRequest(userRequestDTO)
                .flatMap(request -> userRepository.findByUsername(username)
                        .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                        .flatMap(existingUser -> 
                            userRepository.findByUsername(request.getUsername())
                                    .filter(user -> !user.getUsername().equals(username))
                                    .hasElement()
                                    .flatMap(usernameExists -> {
                                        if (usernameExists) {
                                            return Mono.error(new UserAlreadyExistsException(request.getUsername()));
                                        }
                                        return userRepository.findByEmail(request.getEmail())
                                                .filter(user -> !user.getUsername().equals(username))
                                                .hasElement()
                                                .flatMap(emailExists -> {
                                                    if (emailExists) {
                                                        return Mono.error(new UserAlreadyExistsException(request.getEmail()));
                                                    }
                                                    existingUser.setUsername(request.getUsername());
                                                    existingUser.setEmail(request.getEmail());
                                                    if (request.getPassword() != null) {
                                                        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
                                                    }
                                                    return userRepository.save(existingUser)
                                                            .map(this::mapToUserResponse);
                                                });
                                    })))
                .onErrorMap(throwable -> {
                    if (throwable instanceof UserNotFoundException || 
                        throwable instanceof UserAlreadyExistsException ||
                        throwable instanceof RequestValidationException) {
                        return throwable;
                    }
                    return new RuntimeException("Failed to update user: " + throwable.getMessage());
                });
    }

    public Mono<Void> deleteUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Mono.error(new RequestValidationException("Username cannot be empty"));
        }
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                .flatMap(userRepository::delete);
    }

    public Mono<UserResponseDTO> authenticateUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return Mono.error(new RequestValidationException("Username cannot be empty"));
        }
        if (password == null || password.trim().isEmpty()) {
            return Mono.error(new RequestValidationException("Password cannot be empty"));
        }
        
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                .flatMap(user -> {
                    if (passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.just(mapToUserResponse(user));
                    }
                    return Mono.error(new InvalidCredentialsException());
                });
    }

    private Mono<UserRequestDTO> validateUserRequest(UserRequestDTO request) {
        if (request == null) {
            return Mono.error(new RequestValidationException("User request cannot be null"));
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Mono.error(new RequestValidationException("Username cannot be empty"));
        }
        if (!USERNAME_PATTERN.matcher(request.getUsername()).matches()) {
            return Mono.error(new RequestValidationException("Username must be 3-20 characters long and can only contain letters, numbers, underscores, and hyphens"));
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return Mono.error(new RequestValidationException("Email cannot be empty"));
        }
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            return Mono.error(new RequestValidationException("Invalid email format"));
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Mono.error(new RequestValidationException("Password cannot be empty"));
        }
        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            return Mono.error(new RequestValidationException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"));
        }
        return Mono.just(request);
    }

    private UserResponseDTO mapToUserResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(Role.valueOf(user.getRole()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}