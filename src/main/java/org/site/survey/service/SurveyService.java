package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.site.survey.model.Answer;
import org.site.survey.model.Question;
import org.site.survey.model.Survey;
import org.site.survey.repository.AnswerRepository;
import org.site.survey.repository.QuestionRepository;
import org.site.survey.repository.SurveyRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public Flux<Survey> getAllSurveys() {
        return surveyRepository.findAll();
    }

    public Mono<Survey> getSurveyById(Integer id) {
        return surveyRepository.findById(id);
    }

    public Mono<Survey> createSurvey(Survey survey) {
        survey.setCreatedAt(LocalDateTime.now());
        return surveyRepository.save(survey);
    }

    public Flux<Question> getQuestionsBySurveyId(Integer surveyId) {
        return questionRepository.findBySurveyId(surveyId);
    }

    public Mono<Question> createQuestion(Question question) {
        question.setCreatedAt(LocalDateTime.now());
        return questionRepository.save(question);
    }

    public Mono<Answer> submitAnswer(Answer answer) {
        answer.setCreatedAt(LocalDateTime.now());
        return answerRepository.save(answer);
    }

    public Flux<Answer> getAnswersByQuestionId(Integer questionId) {
        return answerRepository.findByQuestionId(questionId);
    }
}