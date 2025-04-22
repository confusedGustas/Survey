package org.site.survey.config;

import lombok.RequiredArgsConstructor;
import org.site.survey.exception.AccessDeniedException;
import org.site.survey.exception.AuthenticationException;
import org.site.survey.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(exceptionHandlingSpec ->
                    exceptionHandlingSpec.authenticationEntryPoint((exchange, ex) ->
                        Mono.error(new AuthenticationException()))
                    .accessDeniedHandler((exchange, denied) ->
                        Mono.error(new AccessDeniedException()))
                ).authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/users")
                        .permitAll()
                        .pathMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                        .anyExchange()
                        .authenticated())
                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
} 