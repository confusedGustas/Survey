package org.site.survey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.site.survey.dto.request.UserRequestDTO;
import org.site.survey.dto.response.UserResponseDTO;
import org.site.survey.exception.ResourceNotFoundException;
import org.site.survey.service.UserService;
import org.site.survey.util.LoggerUtil;
import org.site.survey.util.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    private static final Logger logger = LoggerUtil.getLogger(UserController.class);
    private static final Logger errorLogger = LoggerUtil.getErrorLogger(UserController.class);
    
    private final UserService userService;

    @GetMapping
    @Operation(
        summary = "Get all users",
        description = "Retrieves a list of all users with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "200", description = "User list is or isn't empty"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<Object>> getAllUsers(
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Retrieving all users with pagination - page: {}, size: {}", page, size);
        return ResponseUtils.wrapFluxResponsePaginated(userService.getAllUsers(), "users", page, size)
                .doOnSuccess(response -> logger.info("Successfully retrieved paginated users"))
                .doOnError(error -> {
                    logger.warn("Failed to retrieve paginated users");
                    errorLogger.error("Error retrieving paginated users: {}", error.getMessage(), error);
                });
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves user details by their ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found successfully",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<Object>> getUserById(@PathVariable Integer id) {
        logger.info("Retrieving user by ID: {}", id);
        return userService.getUserById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
                .map(user -> {
                    logger.info("Successfully retrieved user with ID: {}", id);
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "data", user
                    );
                    return ResponseEntity.ok((Object)response);
                })
                .doOnError(error -> {
                    if (error instanceof ResourceNotFoundException) {
                        logger.warn("User with ID {} not found", id);
                    } else {
                        errorLogger.error("Error retrieving user with ID {}: {}", id, error.getMessage(), error);
                    }
                });
    }

    @GetMapping("/username/{username}")
    @Operation(
        summary = "Get user by username",
        description = "Retrieves user details by their username"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found successfully",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<Object>> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
                .map(user -> {
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "data", user
                    );
                    return ResponseEntity.ok(response);
                });
    }

    @PostMapping
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<Object>> createUser(@RequestBody UserRequestDTO userRequestDTO) {
        logger.info("Creating new user with username: {}", userRequestDTO.getUsername());
        return userService.createUser(userRequestDTO)
                .map(user -> {
                    logger.info("Successfully created user with username: {}", user.getUsername());
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "data", user
                    );
                    return ResponseEntity.status(HttpStatus.CREATED).body((Object)response);
                })
                .doOnError(error -> {
                    logger.warn("Failed to create user with username: {}", userRequestDTO.getUsername());
                    errorLogger.error("Error creating user: {}", error.getMessage(), error);
                });
    }

    @PutMapping("/{username}")
    @Operation(
        summary = "Update user details",
        description = "Updates the details of an existing user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden - cannot modify other users"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<Object>> updateUser(
            @Parameter(description = "Username of the user to update", required = true)
            @PathVariable String username,
            @Parameter(description = "Updated user details", required = true)
            @RequestBody UserRequestDTO userRequestDTO) {
        logger.info("Updating user with username: {}", username);
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(currentUsername -> {
                    logger.debug("Current authenticated user: {}, trying to update user: {}", currentUsername, username);
                    return userService.updateUser(username, userRequestDTO, currentUsername);
                })
                .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
                .map(user -> {
                    logger.info("Successfully updated user: {}", username);
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "data", user
                    );
                    return ResponseEntity.ok((Object)response);
                })
                .doOnError(error -> {
                    logger.warn("Failed to update user: {}", username);
                    errorLogger.error("Error updating user {}: {}", username, error.getMessage(), error);
                });
    }

    @DeleteMapping("/{username}")
    @Operation(
        summary = "Delete user",
        description = "Deletes a user by their username"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid username"),
        @ApiResponse(responseCode = "403", description = "Forbidden - cannot delete other users"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<Object>> deleteUser(
            @Parameter(description = "Username of the user to delete", required = true)
            @PathVariable String username) {
        logger.info("Deleting user with username: {}", username);
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(currentUsername -> {
                    logger.debug("Current authenticated user: {}, trying to delete user: {}", currentUsername, username);
                    return userService.deleteUser(username, currentUsername);
                })
                .then(Mono.just(ResponseEntity.ok((Object)Map.of(
                    "status", "success",
                    "message", String.format("User with username '%s' was deleted successfully", username)
                ))))
                .doOnSuccess(response -> logger.info("Successfully deleted user: {}", username))
                .doOnError(error -> {
                    logger.warn("Failed to delete user: {}", username);
                    errorLogger.error("Error deleting user {}: {}", username, error.getMessage(), error);
                });
    }
}