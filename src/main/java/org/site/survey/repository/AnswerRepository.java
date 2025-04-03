package org.site.survey.repository;

import org.site.survey.model.Answer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AnswerRepository extends ReactiveCrudRepository<Answer, Integer> {
    Flux<Answer> findByQuestionId(Integer questionId);
    Flux<Answer> findByUserId(Integer userId);
}