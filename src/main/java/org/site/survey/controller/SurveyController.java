package org.site.survey.controller;

import lombok.RequiredArgsConstructor;
import org.site.survey.model.Answer;
import org.site.survey.model.Question;
import org.site.survey.model.Survey;
import org.site.survey.service.SurveyService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {
    private final SurveyService surveyService;

    @GetMapping
    public Flux<Survey> getAllSurveys() {
        return surveyService.getAllSurveys();
    }

    @GetMapping("/{id}")
    public Mono<Survey> getSurveyById(@PathVariable Integer id) {
        return surveyService.getSurveyById(id);
    }

    @PostMapping
    public Mono<Survey> createSurvey(@RequestBody Survey survey) {
        return surveyService.createSurvey(survey);
    }

    @GetMapping("/{surveyId}/questions")
    public Flux<Question> getQuestionsBySurveyId(@PathVariable Integer surveyId) {
        return surveyService.getQuestionsBySurveyId(surveyId);
    }

    @PostMapping("/{surveyId}/questions")
    public Mono<Question> createQuestion(@PathVariable Integer surveyId, @RequestBody Question question) {
        question.setSurveyId(surveyId);
        return surveyService.createQuestion(question);
    }

    @PostMapping("/questions/{questionId}/answers")
    public Mono<Answer> submitAnswer(@PathVariable Integer questionId, @RequestBody Answer answer) {
        answer.setQuestionId(questionId);
        return surveyService.submitAnswer(answer);
    }

    @GetMapping("/questions/{questionId}/answers")
    public Flux<Answer> getAnswersByQuestionId(@PathVariable Integer questionId) {
        return surveyService.getAnswersByQuestionId(questionId);
    }
}