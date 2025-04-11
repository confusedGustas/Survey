package org.site.survey.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.site.survey.exception.InvalidRefreshTokenException;
import org.site.survey.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import io.jsonwebtoken.ExpiredJwtException;
import reactor.core.publisher.Mono;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationTime;

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaims(token, claims -> claims.get("role", String.class));
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("refresh", false);
        return generateToken(claims, username, expirationTime);
    }

    public String generateRefreshToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("refresh", true);
        return generateToken(claims, username, refreshExpirationTime);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Mono<Map<String, String>> refreshTokens(String refreshToken) {
        try {
            Claims claims;
            try {
                claims = extractAllClaims(refreshToken);
            } catch (ExpiredJwtException e) {
                claims = e.getClaims();
            }

            if (!claims.containsKey("refresh") || !claims.get("refresh", Boolean.class)) {
                return Mono.error(new InvalidRefreshTokenException("Access token cannot be used for refresh"));
            }

            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            if (username == null || role == null) {
                return Mono.error(new InvalidTokenException("Invalid refresh token claims"));
            }

            // Generate new access and refresh tokens
            String newAccessToken = generateToken(username, role);
            String newRefreshToken = generateRefreshToken(username, role);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);

            return Mono.just(tokens);
        } catch (Exception e) {
            return Mono.error(new InvalidTokenException("Invalid refresh token"));
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    private String generateToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 