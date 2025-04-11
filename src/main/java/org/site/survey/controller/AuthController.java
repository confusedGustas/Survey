package org.site.survey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.site.survey.dto.AuthRequestDTO;
import org.site.survey.dto.AuthResponseDTO;
import org.site.survey.exception.AuthenticationException;
import org.site.survey.service.UserService;
import org.site.survey.service.jwt.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for authentication")
public class AuthController {
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
        return userService.authenticateUser(authRequest.getUsername(), authRequest.getPassword())
                .flatMap(user -> {
                    String accessToken = jwtService.generateToken(user.getUsername(), user.getRole());
                    String refreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getRole());
                    return Mono.just(new AuthResponseDTO(accessToken, refreshToken));
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
        return jwtService.refreshTokens(refreshToken)
                .map(tokens -> new AuthResponseDTO(
                    tokens.get("accessToken"),
                    tokens.get("refreshToken")
                ));
    }
} 