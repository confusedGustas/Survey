package org.site.survey.repository.elasticsearch;

import org.site.survey.model.elasticsearch.ChoiceDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public interface ChoiceElasticsearchRepository extends ReactiveElasticsearchRepository<ChoiceDocument, Integer> {
    Flux<ChoiceDocument> findByQuestionId(Integer questionId);
    Flux<ChoiceDocument> findByChoiceTextContaining(String choiceText);
} 