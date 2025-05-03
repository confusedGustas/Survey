package org.site.survey.repository.elasticsearch;

import org.site.survey.model.elasticsearch.SurveyDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
public interface SurveyElasticsearchRepository extends ReactiveElasticsearchRepository<SurveyDocument, Integer> {
    Flux<SurveyDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
    
    @Query("{\"bool\": {\"should\": [{\"match\": {\"title\": \"?0\"}}, {\"match\": {\"description\": \"?0\"}}]}}")
    Flux<SurveyDocument> findByTitleOrDescriptionWithMultiWord(String query);
}