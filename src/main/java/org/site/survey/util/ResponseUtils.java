package org.site.survey.util;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ResponseUtils {

    public static Map<String, Object> emptyResponseMessage(String entity) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "No " + entity + " found");
        response.put("data", Collections.emptyList());
        return response;
    }

    public static <T> Mono<ResponseEntity<Object>> wrapFluxResponsePaginated(Flux<T> flux, String entity, int page, int size) {
        final int requestedPage = Math.max(0, page);
        final int pageSize = (size <= 0) ? 10 : size;
        
        return flux.collectList()
                .map(list -> {
                    if (list.isEmpty()) {
                        return ResponseEntity.ok(emptyResponseMessage(entity));
                    } else {
                        int totalItems = list.size();
                        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
                        
                        final int actualPage = Math.min(requestedPage, Math.max(0, totalPages - 1));
                        
                        int start = actualPage * pageSize;
                        int end = Math.min(start + pageSize, totalItems);
                        
                        List<T> pageItems = (start < totalItems) ? list.subList(start, end) : Collections.emptyList();
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("data", pageItems);
                        
                        Map<String, Object> pagination = new HashMap<>();
                        pagination.put("page", actualPage);
                        pagination.put("size", pageSize);
                        pagination.put("totalItems", totalItems);
                        pagination.put("totalPages", totalPages);
                        
                        response.put("pagination", pagination);
                        return ResponseEntity.ok(response);
                    }
                });
    }
} 