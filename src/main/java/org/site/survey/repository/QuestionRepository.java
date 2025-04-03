package org.site.survey.repository;

import org.site.survey.model.Question;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface QuestionRepository extends ReactiveCrudRepository<Question, Integer> {
    Flux<Question> findBySurveyId(Integer surveyId);
}