package org.site.survey.repository;

import org.site.survey.model.Answer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AnswerRepository extends ReactiveCrudRepository<Answer, Integer> {
    Mono<Boolean> existsByQuestionIdIn(List<Integer> questionIds);
} 