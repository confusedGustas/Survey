package org.site.survey.repository.elasticsearch;

import org.site.survey.model.elasticsearch.QuestionDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public interface QuestionElasticsearchRepository extends ReactiveElasticsearchRepository<QuestionDocument, Integer> {
    Flux<QuestionDocument> findByContentContaining(String content);
    Flux<QuestionDocument> findBySurveyId(Integer surveyId);
    Flux<QuestionDocument> findByQuestionType(String questionType);
} 