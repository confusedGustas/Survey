package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.site.survey.dto.request.UserRequestDTO;
import org.site.survey.dto.response.UserResponseDTO;
import org.site.survey.exception.UserNotFoundException;
import org.site.survey.exception.UserAlreadyExistsException;
import org.site.survey.exception.InvalidCredentialsException;
import org.site.survey.exception.ServiceException;
import org.site.survey.integrity.UserDataIntegrity;
import org.site.survey.mapper.UserMapper;
import org.site.survey.type.RoleType;
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
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Flux<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .map(userMapper::mapToUserResponse);
    }

    public Mono<UserResponseDTO> getUserById(Integer id) {
        userDataIntegrity.validateUserId(id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .map(userMapper::mapToUserResponse);
    }

    public Mono<UserResponseDTO> getUserByUsername(String username) {
        userDataIntegrity.validateUsername(username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .map(userMapper::mapToUserResponse);
    }

    public Mono<UserResponseDTO> createUser(UserRequestDTO userRequestDTO) {
        userDataIntegrity.validateUserRequest(userRequestDTO);
        
        return Mono.just(userRequestDTO)
                .flatMap(request -> userRepository.findByUsername(request.getUsername())
                        .hasElement()
                        .flatMap(usernameExists -> {
                            if (usernameExists) {
                                return Mono.error(new UserAlreadyExistsException());
                            }
                            return userRepository.findByEmail(request.getEmail())
                                    .hasElement()
                                    .flatMap(emailExists -> {
                                        if (emailExists) {
                                            return Mono.error(new UserAlreadyExistsException());
                                        }
                                        User user = User.builder()
                                                .username(request.getUsername())
                                                .email(request.getEmail())
                                                .password(passwordEncoder.encode(request.getPassword()))
                                                .role(String.valueOf(RoleType.USER))
                                                .createdAt(LocalDateTime.now())
                                                .build();
                                        
                                        return userRepository.save(user)
                                                .map(userMapper::mapToUserResponse);
                                    });
                        }))
                .onErrorMap(throwable -> {
                    if (throwable instanceof UserAlreadyExistsException) {
                        return throwable;
                    }
                    return new ServiceException();
                });
    }

    public Mono<UserResponseDTO> updateUser(String username, UserRequestDTO userRequestDTO) {
        userDataIntegrity.validateUsername(username);
        userDataIntegrity.validateUserRequest(userRequestDTO);
        
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .flatMap(existingUser -> 
                    userRepository.findByUsername(userRequestDTO.getUsername())
                            .filter(user -> !user.getUsername().equals(username))
                            .hasElement()
                            .flatMap(usernameExists -> {
                                if (usernameExists) {
                                    return Mono.error(new UserAlreadyExistsException());
                                }
                                return userRepository.findByEmail(userRequestDTO.getEmail())
                                        .filter(user -> !user.getUsername().equals(username))
                                        .hasElement()
                                        .flatMap(emailExists -> {
                                            if (emailExists) {
                                                return Mono.error(new UserAlreadyExistsException());
                                            }
                                            existingUser.setUsername(userRequestDTO.getUsername());
                                            existingUser.setEmail(userRequestDTO.getEmail());
                                            if (userRequestDTO.getPassword() != null) {
                                                existingUser.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
                                            }
                                            return userRepository.save(existingUser)
                                                    .map(userMapper::mapToUserResponse);
                                        });
                            }))
                .onErrorMap(throwable -> {
                    if (throwable instanceof UserNotFoundException || 
                        throwable instanceof UserAlreadyExistsException) {
                        return throwable;
                    }
                    return new ServiceException();
                });
    }

    public Mono<Object> deleteUser(String username) {
        userDataIntegrity.validateUsername(username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .flatMap(userRepository::delete);
    }

    public Mono<User> authenticateUser(String username, String password) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .flatMap(user -> {
                    if (passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.just(user);
                    }
                    return Mono.error(new InvalidCredentialsException());
                });
    }
}