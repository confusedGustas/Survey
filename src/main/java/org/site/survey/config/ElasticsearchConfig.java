package org.site.survey.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
@EnableReactiveElasticsearchRepositories(basePackages = "org.site.survey.repository.elasticsearch")
@Slf4j
public class ElasticsearchConfig extends ReactiveElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;
    
    @Value("${spring.elasticsearch.connection-timeout:10s}")
    private Duration connectTimeout;
    
    @Value("${spring.elasticsearch.socket-timeout:30s}")
    private Duration socketTimeout;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        log.info("Configuring Elasticsearch client with URL: {}", elasticsearchUrl);
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUrl.startsWith("http://") ? elasticsearchUrl.substring(7) : elasticsearchUrl)
                .withConnectTimeout(connectTimeout)
                .withSocketTimeout(socketTimeout)
                .build();
    }
    
    @Bean
    @Override
    @NonNull
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        log.info("Configuring Elasticsearch custom conversions");
        return new ElasticsearchCustomConversions(
            Arrays.asList(
                new LocalDateTimeToStringConverter(),
                new StringToLocalDateTimeConverter()
            )
        );
    }
    
    @WritingConverter
    static class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        @Override
        public String convert(LocalDateTime source) {
            return source.format(FORMATTER);
        }
    }
    
    @ReadingConverter
    static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        @Override
        public LocalDateTime convert(String source) {
            if (source.isEmpty()) {
                return null;
            }

            if (source.length() == 10) {
                source = source + " 00:00:00";
            }
            
            return LocalDateTime.parse(source, FORMATTER);
        }
    }
} 