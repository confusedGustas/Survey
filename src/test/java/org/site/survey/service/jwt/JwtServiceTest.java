package org.site.survey.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.site.survey.exception.InvalidRefreshTokenException;
import org.site.survey.exception.InvalidTokenException;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String SECRET_KEY = "S3cr3tK3yF0rT3st1ngJwtT0k3nsCr34t10n4ndV4l1d4t10n";
    private final long EXPIRATION_TIME = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "expirationTime", EXPIRATION_TIME);
        long REFRESH_EXPIRATION_TIME = 86400000;
        ReflectionTestUtils.setField(jwtService, "refreshExpirationTime", REFRESH_EXPIRATION_TIME);
    }

    @Test
    void generateToken_ValidInput_ReturnsToken() {
        String token = jwtService.generateToken("testuser", "USER");
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        Claims claims = extractClaims(token);
        assertEquals("testuser", claims.getSubject());
        assertEquals("USER", claims.get("role"));
        assertFalse((Boolean) claims.get("refresh"));
    }

    @Test
    void generateRefreshToken_ValidInput_ReturnsRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken("testuser", "USER");
        
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        
        Claims claims = extractClaims(refreshToken);
        assertEquals("testuser", claims.getSubject());
        assertEquals("USER", claims.get("role"));
        assertTrue((Boolean) claims.get("refresh"));
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        String token = jwtService.generateToken("testuser", "USER");
        
        String username = jwtService.extractUsername(token);
        
        assertEquals("testuser", username);
    }

    @Test
    void extractRole_ValidToken_ReturnsRole() {
        String token = jwtService.generateToken("testuser", "ADMIN");
        
        String role = jwtService.extractRole(token);
        
        assertEquals("ADMIN", role);
    }

    @Test
    void isTokenExpired_ExpiredToken_ReturnsTrue() {
        Date issuedAt = new Date(System.currentTimeMillis() - 2 * EXPIRATION_TIME);
        Date expiration = new Date(issuedAt.getTime() + EXPIRATION_TIME);
        
        String token = Jwts.builder()
                .subject("testuser")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                .compact();
        
        assertTrue(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {
        String token = jwtService.generateToken("testuser", "USER");
        
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void refreshTokens_ValidRefreshToken_ReturnsNewTokens() {
        String refreshToken = jwtService.generateRefreshToken("testuser", "USER");
        
        Mono<Map<String, String>> result = jwtService.refreshTokens(refreshToken);
        
        StepVerifier.create(result)
                .assertNext(tokens -> {
                    assertNotNull(tokens.get("accessToken"));
                    assertNotNull(tokens.get("refreshToken"));
                    assertFalse(tokens.get("accessToken").isEmpty());
                    assertFalse(tokens.get("refreshToken").isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void refreshTokens_NonRefreshToken_ReturnsError() {
        String accessToken = jwtService.generateToken("testuser", "USER");
        
        Mono<Map<String, String>> result = jwtService.refreshTokens(accessToken);
        
        StepVerifier.create(result)
                .expectError(InvalidRefreshTokenException.class)
                .verify();
    }

    @Test
    void refreshTokens_InvalidToken_ReturnsError() {
        String invalidToken = "invalidToken";
        
        Mono<Map<String, String>> result = jwtService.refreshTokens(invalidToken);
        
        StepVerifier.create(result)
                .expectError(InvalidTokenException.class)
                .verify();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
} 