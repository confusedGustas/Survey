package org.site.survey.util;

import org.springframework.http.HttpMethod;
import java.util.Arrays;
import java.util.List;

public class SecurityEndpoints {
    public static final List<PublicEndpoint> PUBLIC_ENDPOINTS = Arrays.asList(
        new PublicEndpoint("/auth/login"),
        new PublicEndpoint("/auth/refresh"),
        new PublicEndpoint("/api/users", HttpMethod.POST),
        new PublicEndpoint("/swagger-ui"),
        new PublicEndpoint("/v3/api-docs"),
        new PublicEndpoint("/swagger-resources")
    );

    public static boolean isPublicEndpoint(String path, HttpMethod method) {
        return PUBLIC_ENDPOINTS.stream()
            .anyMatch(endpoint -> endpoint.matches(path, method));
    }
} 