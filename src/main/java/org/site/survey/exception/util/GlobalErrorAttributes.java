package org.site.survey.exception.util;

import org.site.survey.exception.model.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalErrorAttributes.class);
    
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
        Throwable error = getError(request);

        log.error("Error occurred during request processing: ", error);
        
        errorAttributes.put("timestamp", LocalDateTime.now().toString());
        errorAttributes.put("path", request.path());
        
        if (error instanceof BaseException ex) {
            errorAttributes.put("status", ex.getStatus().value());
            errorAttributes.put("error", ex.getStatus().getReasonPhrase());
            errorAttributes.put("errorCode", ex.getErrorCode());
            errorAttributes.put("message", ex.getMessage());
        }
        
        return errorAttributes;
    }
} 