package org.site.survey.repository;

import org.site.survey.model.Choice;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChoiceRepository extends ReactiveCrudRepository<Choice, Integer> {
    Flux<Choice> findByQuestionId(Integer questionId);
    Mono<Long> deleteByQuestionId(Integer questionId);
} 