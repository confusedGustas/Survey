package org.site.survey.util;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResponseUtils {

    public static Map<String, Object> emptyResponseMessage(String entity) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "No " + entity + " found");
        response.put("data", Collections.emptyList());
        return response;
    }

    public static <T> Mono<ResponseEntity<Object>> wrapFluxResponse(Flux<T> flux, String entity) {
        return flux.collectList()
                .map(list -> {
                    if (list.isEmpty()) {
                        return ResponseEntity.ok(emptyResponseMessage(entity));
                    } else {
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("data", list);
                        return ResponseEntity.ok(response);
                    }
                });
    }
} 