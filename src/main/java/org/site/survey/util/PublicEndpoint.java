package org.site.survey.util;

import lombok.Getter;
import org.springframework.http.HttpMethod;

@Getter
public class PublicEndpoint {
    private final String path;
    private final HttpMethod method;

    public PublicEndpoint(String path, HttpMethod method) {
        this.path = path;
        this.method = method;
    }
    
    public PublicEndpoint(String path) {
        this(path, null);
    }
    
    public boolean matches(String requestPath, HttpMethod requestMethod) {
        boolean pathMatches = requestPath.equals(path) || requestPath.startsWith(path + "/");
        return pathMatches && (method == null || method.equals(requestMethod));
    }

} 