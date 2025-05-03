package org.site.survey.service;

import org.apache.logging.log4j.Logger;
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
import org.site.survey.util.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SurveyService {
    
    private static final Logger logger = LoggerUtil.getLogger(SurveyService.class);
    private static final Logger errorLogger = LoggerUtil.getErrorLogger(SurveyService.class);
    
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final AnswerRepository answerRepository;
    private final SurveyDataIntegrity surveyDataIntegrity;
    private final SurveyMapper surveyMapper;
    private ElasticsearchSyncService elasticsearchSyncService;
    
    @Autowired
    public SurveyService(
            SurveyRepository surveyRepository,
            QuestionRepository questionRepository,
            ChoiceRepository choiceRepository,
            AnswerRepository answerRepository,
            SurveyDataIntegrity surveyDataIntegrity,
            SurveyMapper surveyMapper) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
        this.answerRepository = answerRepository;
        this.surveyDataIntegrity = surveyDataIntegrity;
        this.surveyMapper = surveyMapper;
        logger.info("SurveyService initialized");
    }
    
    @Autowired(required = false)
    public void setElasticsearchSyncService(ElasticsearchSyncService elasticsearchSyncService) {
        this.elasticsearchSyncService = elasticsearchSyncService;
        log.info("ElasticsearchSyncService autowired successfully");
        logger.info("ElasticsearchSyncService connected to SurveyService");
    }
    
    private void syncWithElasticsearch() {
        if (elasticsearchSyncService != null) {
            log.debug("Syncing with Elasticsearch after operation");
            logger.debug("Starting Elasticsearch synchronization");
            elasticsearchSyncService.syncAllData()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(v -> {
                    log.debug("Elasticsearch sync completed successfully");
                    logger.info("Elasticsearch synchronization completed");
                })
                .doOnError(e -> {
                    log.error("Error syncing with Elasticsearch: {}", e.getMessage());
                    errorLogger.error("Failed to sync with Elasticsearch: {}", e.getMessage(), e);
                })
                .onErrorComplete()
                .subscribe();
        }
    }
    
    @Transactional
    public Mono<SurveyResponseDTO> createSurvey(SurveyRequestDTO request, Integer userId) {
        logger.info("Creating survey with title '{}' for user ID: {}", request.getTitle(), userId);
        logger.debug("Survey creation request: {}", request);
        
        surveyDataIntegrity.validateSurveyRequest(request);
        surveyDataIntegrity.validateUserId(userId);
        
        Survey newSurvey = Survey.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
        
        logger.debug("Built new survey object: {}", newSurvey);
        
        return surveyRepository.save(newSurvey)
                .flatMap(savedSurvey -> {
                    logger.info("Saved survey with ID: {}", savedSurvey.getId());
                    SurveyResponseDTO response = surveyMapper.mapToSurveyResponse(savedSurvey, null);
                    
                    if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
                        logger.info("Survey has no questions, returning early");
                        return Mono.just(response);
                    }

                    logger.info("Processing {} questions for survey ID: {}", 
                            request.getQuestions().size(), savedSurvey.getId());
                    
                    return Flux.fromIterable(request.getQuestions())
                            .flatMapSequential(questionRequestDTO -> {
                                Integer calculatedSize = (questionRequestDTO.getChoices() != null)
                                        ? questionRequestDTO.getChoices().size()
                                        : 0;
                                
                                logger.debug("Creating question of type {} with {} choices", 
                                        questionRequestDTO.getQuestionType(), calculatedSize);
                                
                                Question newQuestion = Question.builder()
                                        .surveyId(savedSurvey.getId())
                                        .content(questionRequestDTO.getContent())
                                        .questionType(questionRequestDTO.getQuestionType().name())
                                        .questionSize(calculatedSize)
                                        .createdAt(LocalDateTime.now())
                                        .build();
                                
                                return questionRepository.save(newQuestion)
                                        .flatMap(savedQuestion -> {
                                            logger.debug("Saved question with ID: {}", savedQuestion.getId());
                                            
                                            if (questionRequestDTO.getChoices() == null ||
                                                questionRequestDTO.getChoices().isEmpty()) {
                                                logger.debug("Question has no choices, returning early");
                                                return Mono.just(surveyMapper.mapToQuestionResponse(savedQuestion, List.of()));
                                            }
                                            
                                            List<Choice> choices = new ArrayList<>();
                                            for (String choiceText : questionRequestDTO.getChoices()) {
                                                Choice choice = Choice.builder()
                                                        .questionId(savedQuestion.getId())
                                                        .choiceText(choiceText)
                                                        .build();
                                                choices.add(choice);
                                            }
                                            
                                            logger.debug("Created {} choice objects for question ID: {}", 
                                                    choices.size(), savedQuestion.getId());
                                            
                                            return Flux.fromIterable(choices)
                                                    .flatMap(choiceRepository::save)
                                                    .doOnNext(savedChoice -> 
                                                        logger.debug("Saved choice with ID: {}", savedChoice.getId()))
                                                    .map(surveyMapper::mapToChoiceResponse)
                                                    .collectList()
                                                    .map(choiceResponses -> 
                                                            surveyMapper.mapToQuestionResponse(savedQuestion, choiceResponses));
                                        });
                            })
                            .collectList()
                            .map(questionResponses -> {
                                logger.info("Survey creation complete with {} questions", questionResponses.size());
                                response.setQuestions(questionResponses);
                                return response;
                            });
                })
                .doOnSuccess(result -> {
                    logger.info("Survey creation transaction completed successfully");
                    syncWithElasticsearch();
                })
                .doOnError(error -> errorLogger.error("Failed to create survey: {}", error.getMessage(), error));
    }
    
    private Mono<SurveyResponseDTO> mapSurveyWithQuestionsAndChoices(Survey survey) {
        return questionRepository.findBySurveyId(survey.getId())
                .doOnNext(question -> logger.debug("Processing question ID: {} for survey ID: {}", question.getId(), survey.getId()))
                .flatMap(question -> choiceRepository.findByQuestionId(question.getId())
                        .map(surveyMapper::mapToChoiceResponse)
                        .collectList()
                        .map(choices -> surveyMapper.mapToQuestionResponse(question, choices)))
                .collectList()
                .map(questionResponses -> {
                    logger.debug("Mapped survey ID: {} with {} questions", survey.getId(), questionResponses.size());
                    return surveyMapper.mapToSurveyResponse(survey, questionResponses);
                });
    }
    
    public Flux<SurveyResponseDTO> getAllSurveysByUser(Integer userId) {
        logger.info("Retrieving all surveys for user ID: {}", userId);
        surveyDataIntegrity.validateUserId(userId);
        return surveyRepository.findByCreatedBy(userId)
                .doOnNext(SurveyService::foundLog)
                .flatMap(this::mapSurveyWithQuestionsAndChoices)
                .doOnComplete(() -> logger.info("Completed retrieving all surveys for user ID: {}", userId));
    }

    private static void foundLog(Survey survey) {
        logger.debug("Found survey with ID: {}", survey.getId());
    }

    public Flux<SurveyResponseDTO> getAllSurveys() {
        logger.info("Retrieving all surveys");
        return surveyRepository.findAll()
                .doOnNext(SurveyService::foundLog)
                .flatMap(this::mapSurveyWithQuestionsAndChoices)
                .doOnComplete(() -> logger.info("Completed retrieving all surveys"));
    }
    
    @Transactional
    public Mono<Void> deleteSurvey(Integer id, Integer userId) {
        logger.info("Attempting to delete survey ID: {} by user ID: {}", id, userId);
        surveyDataIntegrity.validateSurveyId(id);
        surveyDataIntegrity.validateUserId(userId);
        
        return surveyRepository.findById(id)
                .switchIfEmpty(Mono.error(new SurveyNotFoundException()))
                .flatMap(survey -> {
                    logger.debug("Found survey to delete: {}", survey);
                    
                    if (!survey.getCreatedBy().equals(userId)) {
                        logger.warn("Unauthorized deletion attempt of survey ID: {} by user ID: {}", 
                                id, userId);
                        return Mono.error(new UnauthorizedSurveyAccessException());
                    }

                    return questionRepository.findBySurveyId(id)
                            .map(Question::getId)
                            .collectList()
                            .flatMap(questionIds -> {
                                if (questionIds.isEmpty()) {
                                    logger.info("Survey has no questions, proceeding with deletion");
                                    return surveyRepository.delete(survey);
                                }

                                logger.debug("Found {} questions to check for answers", questionIds.size());
                                return answerRepository.existsByQuestionIdIn(questionIds)
                                        .flatMap(hasAnswers -> {
                                            if (hasAnswers) {
                                                logger.warn("Cannot delete survey ID: {} as it has answers", id);
                                                return Mono.error(new SurveyHasAnswersException());
                                            }

                                            logger.info("Deleting {} questions and their choices for survey ID: {}", 
                                                    questionIds.size(), id);
                                            return Flux.fromIterable(questionIds)
                                                    .flatMap(questionId -> {
                                                        logger.debug("Deleting choices for question ID: {}", questionId);
                                                        return choiceRepository.deleteByQuestionId(questionId);
                                                    })
                                                    .then(questionRepository.deleteBySurveyId(id))
                                                    .then(surveyRepository.delete(survey));
                                        });
                            });
                })
                .doOnSuccess(result -> {
                    logger.info("Successfully deleted survey ID: {}", id);
                    syncWithElasticsearch();
                })
                .doOnError(error -> errorLogger.error("Failed to delete survey ID {}: {}", id, error.getMessage(), error));
    }
} 