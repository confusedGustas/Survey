package org.site.survey.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.Logger;
import org.site.survey.exception.InvalidRefreshTokenException;
import org.site.survey.exception.InvalidTokenException;
import org.site.survey.util.LoggerUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final Logger logger = LoggerUtil.getLogger(JwtService.class);
    private static final Logger errorLogger = LoggerUtil.getErrorLogger(JwtService.class);
    
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationTime;

    public String extractUsername(String token) {
        logger.debug("Extracting username from token");
        return extractClaims(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        logger.debug("Extracting role from token");
        return extractClaims(token, claims -> claims.get("role", String.class));
    }

    public String generateToken(String username, String role) {
        logger.info("Generating access token for user: {}, role: {}", username, role);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("refresh", false);
        String token = generateToken(claims, username, expirationTime);
        logger.debug("Access token generated successfully");
        return token;
    }

    public String generateRefreshToken(String username, String role) {
        logger.info("Generating refresh token for user: {}, role: {}", username, role);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("refresh", true);
        String token = generateToken(claims, username, refreshExpirationTime);
        logger.debug("Refresh token generated successfully");
        return token;
    }

    public boolean isTokenExpired(String token) {
        try {
            boolean expired = extractExpiration(token).before(new Date());
            logger.debug("Token expiration check - expired: {}", expired);
            return expired;
        } catch (Exception e) {
            logger.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public Mono<Map<String, String>> refreshTokens(String refreshToken) {
        logger.info("Attempting to refresh tokens");
        logger.debug("Processing refresh token: {}", refreshToken.substring(0, Math.min(10, refreshToken.length())) + "...");
        
        try {
            Claims claims;
            try {
                claims = extractAllClaims(refreshToken);
                logger.debug("Claims extracted from refresh token");
            } catch (ExpiredJwtException e) {
                logger.warn("Refresh token has expired, but proceeding with claims extraction");
                claims = e.getClaims();
            }

            if (!claims.containsKey("refresh") || !claims.get("refresh", Boolean.class)) {
                logger.warn("Invalid refresh token - not a refresh token");
                return Mono.error(new InvalidRefreshTokenException());
            }

            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            if (username == null || role == null) {
                logger.warn("Invalid refresh token - missing username or role");
                return Mono.error(new InvalidTokenException());
            }

            logger.info("Generating new tokens for user: {}", username);
            String newAccessToken = generateToken(username, role);
            String newRefreshToken = generateRefreshToken(username, role);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);

            logger.info("Tokens refreshed successfully for user: {}", username);
            return Mono.just(tokens);
        } catch (Exception e) {
            errorLogger.error("Error refreshing tokens: {}", e.getMessage(), e);
            return Mono.error(new InvalidTokenException());
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            logger.debug("Extracting all claims from token");
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("Token has expired, but extracting claims anyway");
            return e.getClaims();
        } catch (Exception e) {
            logger.warn("Error extracting claims from token: {}", e.getMessage());
            throw new InvalidTokenException();
        }
    }

    private String generateToken(Map<String, Object> claims, String subject, long expiration) {
        logger.debug("Building JWT for subject: {}, expiration: {} ms", subject, expiration);
        Date now = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        logger.debug("Extracting specific claim from token");
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date extractExpiration(String token) {
        logger.debug("Extracting expiration date from token");
        return extractClaims(token, Claims::getExpiration);
    }

    private SecretKey getSignInKey() {
        logger.debug("Retrieving signing key for JWT");
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 