package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.site.survey.dto.UserRequestDTO;
import org.site.survey.dto.UserResponseDTO;
import org.site.survey.exception.UserNotFoundException;
import org.site.survey.exception.UserAlreadyExistsException;
import org.site.survey.integrity.UserDataIntegrity;
import org.site.survey.type.Role;
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
    private final UserDataIntegrity userDataIntegrity;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Flux<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponseDTO> getUserById(Integer id) {
        userDataIntegrity.validateUserId(id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(String.valueOf(id))))
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponseDTO> getUserByUsername(String username) {
        userDataIntegrity.validateUsername(username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponseDTO> createUser(UserRequestDTO userRequestDTO) {
        userDataIntegrity.validateUserRequest(userRequestDTO);
        
        return Mono.just(userRequestDTO)
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
                    if (throwable instanceof UserAlreadyExistsException) {
                        return throwable;
                    }
                    return new RuntimeException("Failed to create user: " + throwable.getMessage());
                });
    }

    public Mono<UserResponseDTO> updateUser(String username, UserRequestDTO userRequestDTO) {
        userDataIntegrity.validateUsername(username);
        userDataIntegrity.validateUserRequest(userRequestDTO);
        
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                .flatMap(existingUser -> 
                    userRepository.findByUsername(userRequestDTO.getUsername())
                            .filter(user -> !user.getUsername().equals(username))
                            .hasElement()
                            .flatMap(usernameExists -> {
                                if (usernameExists) {
                                    return Mono.error(new UserAlreadyExistsException(userRequestDTO.getUsername()));
                                }
                                return userRepository.findByEmail(userRequestDTO.getEmail())
                                        .filter(user -> !user.getUsername().equals(username))
                                        .hasElement()
                                        .flatMap(emailExists -> {
                                            if (emailExists) {
                                                return Mono.error(new UserAlreadyExistsException(userRequestDTO.getEmail()));
                                            }
                                            existingUser.setUsername(userRequestDTO.getUsername());
                                            existingUser.setEmail(userRequestDTO.getEmail());
                                            if (userRequestDTO.getPassword() != null) {
                                                existingUser.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
                                            }
                                            return userRepository.save(existingUser)
                                                    .map(this::mapToUserResponse);
                                        });
                            }))
                .onErrorMap(throwable -> {
                    if (throwable instanceof UserNotFoundException || 
                        throwable instanceof UserAlreadyExistsException) {
                        return throwable;
                    }
                    return new RuntimeException("Failed to update user: " + throwable.getMessage());
                });
    }

    public Mono<Object> deleteUser(String username) {
        userDataIntegrity.validateUsername(username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                .flatMap(userRepository::delete);
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