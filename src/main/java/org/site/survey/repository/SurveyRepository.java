package org.site.survey.repository;

import org.site.survey.model.Survey;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SurveyRepository extends ReactiveCrudRepository<Survey, Integer> {
    Flux<Survey> findByCreatedBy(Integer userId);
}