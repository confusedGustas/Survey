package org.site.survey.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.site.survey.service.ElasticsearchSyncService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnBean(ElasticsearchSyncService.class)
public class ElasticsearchSyncConfig {

    private final ElasticsearchSyncService elasticsearchSyncService;

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationStart() {
        log.info("Application started, will initialize Elasticsearch sync after delay...");
        // Delay initial sync to ensure Elasticsearch is ready
        Mono.delay(Duration.ofSeconds(30))
            .then(elasticsearchSyncService.syncAllData())
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                .maxBackoff(Duration.ofSeconds(30))
                .doBeforeRetry(retrySignal -> 
                    log.warn("Retrying Elasticsearch sync attempt {} after failure", 
                        retrySignal.totalRetries() + 1)))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSuccess(v -> log.info("Initial Elasticsearch sync completed successfully"))
            .doOnError(e -> log.error("Error during initial Elasticsearch sync: {}", e.getMessage()))
            .onErrorComplete() // Don't fail application startup on sync failure
            .subscribe();
    }

    @Bean
    public ElasticsearchSyncScheduler elasticsearchSyncScheduler() {
        return new ElasticsearchSyncScheduler(elasticsearchSyncService);
    }

    /**
     * A scheduler that periodically syncs data to Elasticsearch
     */
    @RequiredArgsConstructor
    @Slf4j
    public static class ElasticsearchSyncScheduler {
        private final ElasticsearchSyncService syncService;
        private final Duration syncInterval = Duration.ofHours(1);

        @Bean
        public Mono<Void> scheduleSync() {
            return Mono.delay(syncInterval)
                    .flatMap(l -> syncService.syncAllData()
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                                .maxBackoff(Duration.ofSeconds(30)))
                            .doOnSuccess(v -> log.info("Scheduled Elasticsearch sync completed"))
                            .doOnError(e -> log.error("Error during scheduled Elasticsearch sync: {}", e.getMessage()))
                            .onErrorComplete()) // Don't fail the scheduling on sync failure
                    .then(scheduleSync())
                    .subscribeOn(Schedulers.boundedElastic());
        }
    }
} 