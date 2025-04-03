package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.site.survey.model.Answer;
import org.site.survey.model.Question;
import org.site.survey.repository.AnswerRepository;
import org.site.survey.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public Flux<Question> searchQuestions(String content) {
        return questionRepository.findAll()
                .filter(question -> question.getContent().toLowerCase().contains(content.toLowerCase()));
    }

    public Flux<Answer> searchAnswers(String content) {
        return answerRepository.findAll()
                .filter(answer -> answer.getResponse() != null &&
                        answer.getResponse().toLowerCase().contains(content.toLowerCase()));
    }
}