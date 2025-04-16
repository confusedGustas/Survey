package org.site.survey.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.beans.factory.annotation.Value;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * Elasticsearch configuration that is only active when elasticsearch.enabled=true
 */
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
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
    public ClientConfiguration clientConfiguration() {
        log.info("Configuring Elasticsearch client with URL: {}", elasticsearchUrl);
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUrl.replace("http://", ""))
                .withConnectTimeout(connectTimeout)
                .withSocketTimeout(socketTimeout)
                .build();
    }
} 