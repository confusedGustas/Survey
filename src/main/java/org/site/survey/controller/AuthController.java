package org.site.survey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.site.survey.dto.request.AuthRequestDTO;
import org.site.survey.dto.response.AuthResponseDTO;
import org.site.survey.exception.AuthenticationException;
import org.site.survey.service.UserService;
import org.site.survey.service.jwt.JwtService;
import org.site.survey.util.LoggerUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for authentication")
public class AuthController {
    private static final Logger logger = LoggerUtil.getLogger(AuthController.class);
    private static final Logger errorLogger = LoggerUtil.getErrorLogger(AuthController.class);
    
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(
        summary = "Login user",
        description = "Authenticates a user and returns JWT tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequest) {
        logger.info("Login attempt for user: {}", authRequest.getUsername());
        
        return userService.authenticateUser(authRequest.getUsername(), authRequest.getPassword())
                .flatMap(user -> {
                    logger.info("User authenticated successfully: {}", user.getUsername());
                    logger.debug("Generating tokens for user: {}, role: {}", user.getUsername(), user.getRole());
                    
                    String accessToken = jwtService.generateToken(user.getUsername(), user.getRole());
                    String refreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getRole());
                    
                    logger.debug("Tokens generated successfully for user: {}", user.getUsername());
                    return Mono.just(new AuthResponseDTO(accessToken, refreshToken));
                })
                .doOnError(e -> {
                    logger.warn("Authentication failed for user: {}", authRequest.getUsername());
                    errorLogger.error("Authentication error: {}", e.getMessage(), e);
                })
                .onErrorMap(e -> new AuthenticationException());
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh tokens",
        description = "Generates new access and refresh tokens using the refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid refresh request"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public Mono<AuthResponseDTO> refreshToken(@RequestParam String refreshToken) {
        logger.info("Token refresh request received");
        logger.debug("Processing refresh token: {}", refreshToken.substring(0, Math.min(10, refreshToken.length())) + "...");
        
        return jwtService.refreshTokens(refreshToken)
                .map(tokens -> {
                    logger.info("Tokens refreshed successfully");
                    return new AuthResponseDTO(
                        tokens.get("accessToken"),
                        tokens.get("refreshToken")
                    );
                })
                .doOnError(e -> {
                    logger.warn("Token refresh failed");
                    errorLogger.error("Token refresh error: {}", e.getMessage(), e);
                });
    }

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint", description = "A simple health check endpoint that doesn't require authentication")
    public Mono<Map<String, String>> healthCheck() {
        return Mono.just(Map.of("status", "UP"));
    }
} 