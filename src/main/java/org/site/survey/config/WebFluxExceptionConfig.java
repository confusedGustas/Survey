package org.site.survey.config;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebFluxExceptionConfig {
    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }
    
    @Bean
    public WebProperties webProperties() {
        return new WebProperties();
    }

    @Configuration
    public static class WebFluxConfig implements WebFluxConfigurer {
        @Override
        public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
            configurer.defaultCodecs().maxInMemorySize(512 * 1024);
        }
    }
} 