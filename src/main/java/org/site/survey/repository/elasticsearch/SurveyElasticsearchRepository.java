package org.site.survey.repository.elasticsearch;

import org.site.survey.model.elasticsearch.SurveyDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public interface SurveyElasticsearchRepository extends ReactiveElasticsearchRepository<SurveyDocument, Integer> {
    Flux<SurveyDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
} 