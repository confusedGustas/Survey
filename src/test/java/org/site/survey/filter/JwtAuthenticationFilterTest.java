package org.site.survey.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.site.survey.exception.InvalidTokenException;
import org.site.survey.model.User;
import org.site.survey.repository.UserRepository;
import org.site.survey.service.jwt.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private JwtService jwtService;
    private UserRepository userRepository;
    private WebFilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = Mockito.mock(JwtService.class);
        userRepository = Mockito.mock(UserRepository.class);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userRepository);
        filterChain = Mockito.mock(WebFilterChain.class);

        when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_PublicEndpoint_PassesThrough() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest
                        .method(HttpMethod.POST, "/auth/login")
                        .build()
        );

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_NoAuthHeader_PassesThrough() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest
                        .method(HttpMethod.GET, "/api/users")
                        .build()
        );

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_ValidToken_AuthenticatesRequest() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest
                        .method(HttpMethod.GET, "/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                        .build()
        );

        when(jwtService.isTokenExpired("valid-token")).thenReturn(false);
        when(jwtService.extractUsername("valid-token")).thenReturn("testuser");
        when(jwtService.extractRole("valid-token")).thenReturn("USER");
        
        // Mock the user repository
        User mockUser = new User();
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(mockUser));

        WebFilterChain customFilterChain = exchange1 -> Mono.deferContextual(ctx -> {
            if (ctx.hasKey(ReactiveSecurityContextHolder.class.getName())) {
                Mono<SecurityContext> securityContextMono = ctx.get(ReactiveSecurityContextHolder.class.getName());

                return securityContextMono
                    .doOnNext(securityContext -> {
                        Authentication authentication = securityContext.getAuthentication();
                        assertEquals("testuser", authentication.getPrincipal());
                        assertEquals("ROLE_USER", authentication.getAuthorities().iterator().next().getAuthority());
                    })
                    .then(Mono.empty());
            }
            return Mono.empty();
        });

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, customFilterChain);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_ExpiredToken_ReturnsUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest
                        .method(HttpMethod.GET, "/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer expired-token")
                        .build()
        );

        when(jwtService.isTokenExpired("expired-token")).thenReturn(true);

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectError(InvalidTokenException.class)
                .verify();
    }

    @Test
    void filter_InvalidToken_ReturnsUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest
                        .method(HttpMethod.GET, "/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                        .build()
        );

        when(jwtService.isTokenExpired("invalid-token")).thenReturn(false);
        when(jwtService.extractUsername("invalid-token")).thenReturn(null);

        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .expectError(InvalidTokenException.class)
                .verify();
    }
} 