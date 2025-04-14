package org.site.survey.filter;

import lombok.RequiredArgsConstructor;
import org.site.survey.exception.AuthenticationException;
import org.site.survey.exception.InvalidTokenException;
import org.site.survey.repository.UserRepository;
import org.site.survey.service.jwt.JwtService;
import org.site.survey.util.SecurityEndpoints;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (SecurityEndpoints.isPublicEndpoint(request.getPath().value(), request.getMethod())) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);
        
        return Mono.defer(() -> {
            if (jwtService.isTokenExpired(token)) {
                return Mono.error(new InvalidTokenException());
            }

            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);

            if (username == null || role == null) {
                return Mono.error(new InvalidTokenException());
            }

            return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new AuthenticationException()))
                .flatMap(user -> {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        user, // Use the entire User object as the principal
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    SecurityContext securityContext = new SecurityContextImpl(auth);

                    return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
                });
        }).onErrorResume(e -> {
            if (e instanceof InvalidTokenException) {
                return Mono.error(e);
            }
            return Mono.error(new AuthenticationException());
        });
    }
} 