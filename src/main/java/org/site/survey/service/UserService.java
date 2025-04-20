package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.site.survey.dto.request.UserRequestDTO;
import org.site.survey.dto.response.UserResponseDTO;
import org.site.survey.exception.UserNotFoundException;
import org.site.survey.exception.UserAlreadyExistsException;
import org.site.survey.exception.InvalidCredentialsException;
import org.site.survey.exception.ServiceException;
import org.site.survey.exception.UnauthorizedUserModificationException;
import org.site.survey.integrity.UserDataIntegrity;
import org.site.survey.mapper.UserMapper;
import org.site.survey.type.RoleType;
import org.site.survey.model.User;
import org.site.survey.repository.UserRepository;
import org.site.survey.util.LoggerUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LoggerUtil.getLogger(UserService.class);
    private static final Logger errorLogger = LoggerUtil.getErrorLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserDataIntegrity userDataIntegrity;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Flux<UserResponseDTO> getAllUsers() {
        logger.info("Retrieving all users");
        return userRepository.findAll()
                .map(userMapper::mapToUserResponse)
                .doOnComplete(() -> logger.info("Successfully retrieved all users"))
                .doOnError(e -> {
                    logger.warn("Error retrieving all users");
                    errorLogger.error("Failed to retrieve all users: {}", e.getMessage(), e);
                });
    }

    public Mono<UserResponseDTO> getUserById(Integer id) {
        logger.info("Retrieving user by ID: {}", id);
        userDataIntegrity.validateUserId(id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .map(userMapper::mapToUserResponse)
                .doOnSuccess(user -> logger.info("Successfully retrieved user with ID: {}", id))
                .doOnError(e -> {
                    if (e instanceof UserNotFoundException) {
                        logger.warn("User with ID {} not found", id);
                    } else {
                        errorLogger.error("Error retrieving user with ID {}: {}", id, e.getMessage(), e);
                    }
                });
    }

    public Mono<UserResponseDTO> getUserByUsername(String username) {
        logger.info("Retrieving user by username: {}", username);
        userDataIntegrity.validateUsername(username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .map(userMapper::mapToUserResponse)
                .doOnSuccess(user -> logger.info("Successfully retrieved user with username: {}", username))
                .doOnError(e -> {
                    if (e instanceof UserNotFoundException) {
                        logger.warn("User with username {} not found", username);
                    } else {
                        errorLogger.error("Error retrieving user with username {}: {}", username, e.getMessage(), e);
                    }
                });
    }

    public Mono<UserResponseDTO> createUser(UserRequestDTO userRequestDTO) {
        logger.info("Creating new user with username: {}", userRequestDTO.getUsername());
        userDataIntegrity.validateUserRequest(userRequestDTO);
        
        return Mono.just(userRequestDTO)
                .flatMap(request -> userRepository.findByUsername(request.getUsername())
                        .hasElement()
                        .flatMap(usernameExists -> {
                            if (usernameExists) {
                                logger.warn("Username already exists: {}", request.getUsername());
                                return Mono.error(new UserAlreadyExistsException());
                            }
                            return userRepository.findByEmail(request.getEmail())
                                    .hasElement()
                                    .flatMap(emailExists -> {
                                        if (emailExists) {
                                            logger.warn("Email already exists: {}", request.getEmail());
                                            return Mono.error(new UserAlreadyExistsException());
                                        }
                                        logger.debug("Creating user entity with username: {}", request.getUsername());
                                        User user = User.builder()
                                                .username(request.getUsername())
                                                .email(request.getEmail())
                                                .password(passwordEncoder.encode(request.getPassword()))
                                                .role(String.valueOf(RoleType.USER))
                                                .createdAt(LocalDateTime.now())
                                                .build();
                                        
                                        return userRepository.save(user)
                                                .map(userMapper::mapToUserResponse)
                                                .doOnSuccess(u -> logger.info("Successfully created user with ID: {}", u.getId()));
                                    });
                        }))
                .onErrorMap(throwable -> {
                    if (throwable instanceof UserAlreadyExistsException) {
                        return throwable;
                    }
                    errorLogger.error("Failed to create user: {}", throwable.getMessage(), throwable);
                    return new ServiceException();
                });
    }

    public Mono<UserResponseDTO> updateUser(String username, UserRequestDTO userRequestDTO, String currentUsername) {
        logger.info("Updating user with username: {}", username);
        userDataIntegrity.validateUsername(username);
        userDataIntegrity.validateUserRequest(userRequestDTO);
        
        if (isAuthorizedToModifyUser(username, currentUsername)) {
            logger.warn("Unauthorized attempt to update user: {} by {}", username, currentUsername);
            return Mono.error(new UnauthorizedUserModificationException());
        }
        
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .flatMap(existingUser -> 
                    userRepository.findByUsername(userRequestDTO.getUsername())
                            .filter(user -> !user.getUsername().equals(username))
                            .hasElement()
                            .flatMap(usernameExists -> {
                                if (usernameExists) {
                                    logger.warn("Cannot update to username that already exists: {}", userRequestDTO.getUsername());
                                    return Mono.error(new UserAlreadyExistsException());
                                }
                                return userRepository.findByEmail(userRequestDTO.getEmail())
                                        .filter(user -> !user.getUsername().equals(username))
                                        .hasElement()
                                        .flatMap(emailExists -> {
                                            if (emailExists) {
                                                logger.warn("Cannot update to email that already exists: {}", userRequestDTO.getEmail());
                                                return Mono.error(new UserAlreadyExistsException());
                                            }
                                            logger.debug("Updating user entity: {}", username);
                                            existingUser.setUsername(userRequestDTO.getUsername());
                                            existingUser.setEmail(userRequestDTO.getEmail());
                                            if (userRequestDTO.getPassword() != null) {
                                                existingUser.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
                                            }
                                            return userRepository.save(existingUser)
                                                    .map(userMapper::mapToUserResponse)
                                                    .doOnSuccess(u -> logger.info("Successfully updated user: {} to {}", username, u.getUsername()));
                                        });
                            }))
                .onErrorMap(throwable -> {
                    if (throwable instanceof UserNotFoundException || 
                        throwable instanceof UserAlreadyExistsException ||
                        throwable instanceof UnauthorizedUserModificationException) {
                        return throwable;
                    }
                    errorLogger.error("Failed to update user {}: {}", username, throwable.getMessage(), throwable);
                    return new ServiceException();
                });
    }

    public Mono<Object> deleteUser(String username, String currentUsername) {
        logger.info("Deleting user with username: {}", username);
        userDataIntegrity.validateUsername(username);
        
        if (isAuthorizedToModifyUser(username, currentUsername)) {
            logger.warn("Unauthorized attempt to delete user: {} by {}", username, currentUsername);
            return Mono.error(new UnauthorizedUserModificationException());
        }
        
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .flatMap(user -> {
                    logger.debug("Deleting user entity: {}", username);
                    return userRepository.delete(user)
                            .then(Mono.just((Object)"User deleted"));
                })
                .doOnSuccess(v -> logger.info("Successfully deleted user: {}", username))
                .doOnError(e -> {
                    if (e instanceof UserNotFoundException) {
                        logger.warn("User with username {} not found for deletion", username);
                    } else {
                        errorLogger.error("Error deleting user {}: {}", username, e.getMessage(), e);
                    }
                });
    }

    public Mono<User> authenticateUser(String username, String password) {
        logger.info("Authenticating user: {}", username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .flatMap(user -> {
                    if (passwordEncoder.matches(password, user.getPassword())) {
                        logger.info("User authenticated successfully: {}", username);
                        return Mono.just(user);
                    }
                    return Mono.error(new InvalidCredentialsException());
                })
                .doOnError(e -> {
                    if (e instanceof UserNotFoundException) {
                        logger.warn("User not found during authentication: {}", username);
                    } else if (e instanceof InvalidCredentialsException) {
                        logger.warn("Invalid credentials for user: {}", username);
                    } else {
                        errorLogger.error("Error during authentication for user {}: {}", username, e.getMessage(), e);
                    }
                });
    }
    
    private boolean isAuthorizedToModifyUser(String targetUsername, String currentUsername) {
        return !targetUsername.equals(currentUsername);
    }
}