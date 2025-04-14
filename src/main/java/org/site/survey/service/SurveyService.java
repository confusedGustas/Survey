package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.site.survey.dto.request.SurveyRequestDTO;
import org.site.survey.dto.response.SurveyResponseDTO;
import org.site.survey.exception.SurveyHasAnswersException;
import org.site.survey.exception.SurveyNotFoundException;
import org.site.survey.exception.UnauthorizedSurveyAccessException;
import org.site.survey.integrity.SurveyDataIntegrity;
import org.site.survey.mapper.SurveyMapper;
import org.site.survey.model.Choice;
import org.site.survey.model.Question;
import org.site.survey.model.Survey;
import org.site.survey.repository.AnswerRepository;
import org.site.survey.repository.ChoiceRepository;
import org.site.survey.repository.QuestionRepository;
import org.site.survey.repository.SurveyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyService {
    
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final AnswerRepository answerRepository;
    private final SurveyDataIntegrity surveyDataIntegrity;
    private final SurveyMapper surveyMapper;
    
    @Transactional
    public Mono<SurveyResponseDTO> createSurvey(SurveyRequestDTO request, Integer userId) {
        surveyDataIntegrity.validateSurveyRequest(request);
        surveyDataIntegrity.validateUserId(userId);
        
        Survey newSurvey = Survey.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
        
        return surveyRepository.save(newSurvey)
                .flatMap(savedSurvey -> {
                    SurveyResponseDTO response = surveyMapper.mapToSurveyResponse(savedSurvey, null);
                    
                    if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
                        return Mono.just(response);
                    }

                    return Flux.fromIterable(request.getQuestions())
                            .flatMapSequential(questionRequestDTO -> {
                                Integer calculatedSize = (questionRequestDTO.getChoices() != null)
                                        ? questionRequestDTO.getChoices().size()
                                        : 0;
                                
                                Question newQuestion = Question.builder()
                                        .surveyId(savedSurvey.getId())
                                        .content(questionRequestDTO.getContent())
                                        .questionType(questionRequestDTO.getQuestionType().name())
                                        .questionSize(calculatedSize)
                                        .createdAt(LocalDateTime.now())
                                        .build();
                                
                                return questionRepository.save(newQuestion)
                                        .flatMap(savedQuestion -> {
                                            if (questionRequestDTO.getChoices() == null ||
                                                questionRequestDTO.getChoices().isEmpty()) {
                                                return Mono.just(surveyMapper.mapToQuestionResponse(savedQuestion, null));
                                            }
                                            
                                            List<Choice> choices = new ArrayList<>();
                                            for (String choiceText : questionRequestDTO.getChoices()) {
                                                Choice choice = Choice.builder()
                                                        .questionId(savedQuestion.getId())
                                                        .choiceText(choiceText)
                                                        .build();
                                                choices.add(choice);
                                            }
                                            
                                            return Flux.fromIterable(choices)
                                                    .flatMap(choiceRepository::save)
                                                    .map(surveyMapper::mapToChoiceResponse)
                                                    .collectList()
                                                    .map(choiceResponses -> 
                                                            surveyMapper.mapToQuestionResponse(savedQuestion, choiceResponses));
                                        });
                            })
                            .collectList()
                            .map(questionResponses -> {
                                response.setQuestions(questionResponses);
                                return response;
                            });
                });
    }
    
    public Flux<SurveyResponseDTO> getAllSurveysByUser(Integer userId) {
        surveyDataIntegrity.validateUserId(userId);
        
        return surveyRepository.findByCreatedBy(userId)
                .flatMap(survey -> questionRepository.findBySurveyId(survey.getId())
                        .flatMap(question -> choiceRepository.findByQuestionId(question.getId())
                                .map(surveyMapper::mapToChoiceResponse)
                                .collectList()
                                .map(choices -> surveyMapper.mapToQuestionResponse(question, choices)))
                        .collectList()
                        .map(questionResponses -> surveyMapper.mapToSurveyResponse(survey, questionResponses)));
    }
    
    @Transactional
    public Mono<Void> deleteSurvey(Integer id, Integer userId) {
        surveyDataIntegrity.validateSurveyId(id);
        surveyDataIntegrity.validateUserId(userId);
        
        return surveyRepository.findById(id)
                .switchIfEmpty(Mono.error(new SurveyNotFoundException()))
                .flatMap(survey -> {
                    if (!survey.getCreatedBy().equals(userId)) {
                        return Mono.error(new UnauthorizedSurveyAccessException());
                    }

                    return questionRepository.findBySurveyId(id)
                            .map(Question::getId)
                            .collectList()
                            .flatMap(questionIds -> {
                                if (questionIds.isEmpty()) {
                                    return surveyRepository.delete(survey);
                                }

                                return answerRepository.existsByQuestionIdIn(questionIds)
                                        .flatMap(hasAnswers -> {
                                            if (hasAnswers) {
                                                return Mono.error(new SurveyHasAnswersException());
                                            }

                                            return Flux.fromIterable(questionIds)
                                                    .flatMap(choiceRepository::deleteByQuestionId)
                                                    .then(questionRepository.deleteBySurveyId(id))
                                                    .then(surveyRepository.delete(survey));
                                        });
                            });
                });
    }
} 