package org.site.survey.repository.elasticsearch;

import org.site.survey.model.elasticsearch.AnswerDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
public interface AnswerElasticsearchRepository extends ReactiveElasticsearchRepository<AnswerDocument, Integer> {
    Flux<AnswerDocument> findByQuestionId(Integer questionId);
    Flux<AnswerDocument> findByUserId(Integer userId);
    Flux<AnswerDocument> findByIsPublic(Boolean isPublic);
    Flux<AnswerDocument> findByQuestionIdAndUserId(Integer questionId, Integer userId);
} 