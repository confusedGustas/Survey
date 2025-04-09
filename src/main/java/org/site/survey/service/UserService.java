package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.site.survey.dto.request.UserRequest;
import org.site.survey.dto.response.UserResponse;
import org.site.survey.exception.ResourceNotFoundException;
import org.site.survey.exception.ValidationException;
import org.site.survey.model.Role;
import org.site.survey.model.User;
import org.site.survey.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Flux<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponse> getUserById(Integer id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "id", id)))
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "username", username)))
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponse> createUser(UserRequest userRequest) {
        return Mono.just(userRequest)
                .flatMap(request -> userRepository.findByUsername(request.getUsername())
                        .hasElement()
                        .flatMap(usernameExists -> {
                            if (usernameExists) {
                                return Mono.error(new ValidationException("Username already exists"));
                            }
                            return userRepository.findByEmail(request.getEmail())
                                    .hasElement()
                                    .flatMap(emailExists -> {
                                        if (emailExists) {
                                            return Mono.error(new ValidationException("Email already exists"));
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
                    if (throwable instanceof ValidationException) {
                        return throwable;
                    }
                    return new ValidationException("Failed to create user: " + throwable.getMessage());
                });
    }

    public Mono<UserResponse> updateUser(String username, UserRequest userRequest) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "username", username)))
                .flatMap(existingUser -> 
                    userRepository.findByUsername(userRequest.getUsername())
                            .filter(user -> !user.getUsername().equals(username))
                            .hasElement()
                            .flatMap(usernameExists -> {
                                if (usernameExists) {
                                    return Mono.error(new ValidationException("Username already exists"));
                                }
                                return userRepository.findByEmail(userRequest.getEmail())
                                        .filter(user -> !user.getUsername().equals(username))
                                        .hasElement()
                                        .flatMap(emailExists -> {
                                            if (emailExists) {
                                                return Mono.error(new ValidationException("Email already exists"));
                                            }
                                            existingUser.setUsername(userRequest.getUsername());
                                            existingUser.setEmail(userRequest.getEmail());
                                            if (userRequest.getPassword() != null) {
                                                existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
                                            }
                                            return userRepository.save(existingUser)
                                                    .map(this::mapToUserResponse);
                                        });
                            }))
                .onErrorMap(throwable -> {
                    if (throwable instanceof ValidationException || throwable instanceof ResourceNotFoundException) {
                        return throwable;
                    }
                    return new ValidationException("Failed to update user: " + throwable.getMessage());
                });
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(Role.valueOf(user.getRole()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}