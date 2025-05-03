package org.site.survey.service;

import org.site.survey.dto.StatisticsDTO;
import org.site.survey.dto.response.SearchResultDTO;
import org.site.survey.integrity.ElasticsearchDataIntegrity;
import org.site.survey.mapper.ElasticsearchMapper;
import org.site.survey.model.Answer;
import org.site.survey.model.Question;
import org.site.survey.model.elasticsearch.AnswerDocument;
import org.site.survey.model.elasticsearch.ChoiceDocument;
import org.site.survey.model.elasticsearch.QuestionDocument;
import org.site.survey.model.elasticsearch.SurveyDocument;
import org.site.survey.repository.AnswerRepository;
import org.site.survey.repository.ChoiceRepository;
import org.site.survey.repository.QuestionRepository;
import org.site.survey.repository.SurveyRepository;
import org.site.survey.repository.UserRepository;
import org.site.survey.repository.elasticsearch.AnswerElasticsearchRepository;
import org.site.survey.repository.elasticsearch.ChoiceElasticsearchRepository;
import org.site.survey.repository.elasticsearch.QuestionElasticsearchRepository;
import org.site.survey.repository.elasticsearch.SurveyElasticsearchRepository;
import org.site.survey.util.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class AdminService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ElasticsearchDataIntegrity elasticsearchDataIntegrity;
    private final ElasticsearchMapper elasticsearchMapper;
    private static final Logger logger = LoggerUtil.getLogger(AdminService.class);
    private static final Logger errorLogger = LoggerUtil.getErrorLogger(AdminService.class);

    private SurveyElasticsearchRepository surveyElasticsearchRepository;
    private QuestionElasticsearchRepository questionElasticsearchRepository;
    private ChoiceElasticsearchRepository choiceElasticsearchRepository;
    private AnswerElasticsearchRepository answerElasticsearchRepository;
    
    @Autowired
    public AdminService(
            SurveyRepository surveyRepository,
            QuestionRepository questionRepository,
            ChoiceRepository choiceRepository,
            AnswerRepository answerRepository,
            UserRepository userRepository,
            ElasticsearchDataIntegrity elasticsearchDataIntegrity,
            ElasticsearchMapper elasticsearchMapper) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.elasticsearchDataIntegrity = elasticsearchDataIntegrity;
        this.elasticsearchMapper = elasticsearchMapper;
        logger.info("AdminService initialized");
    }
    
    @Autowired(required = false)
    @ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
    public void setElasticsearchDependencies(
            ReactiveElasticsearchOperations operations,
            SurveyElasticsearchRepository surveyElasticsearchRepository,
            QuestionElasticsearchRepository questionElasticsearchRepository,
            ChoiceElasticsearchRepository choiceElasticsearchRepository,
            AnswerElasticsearchRepository answerElasticsearchRepository) {
        
        if (operations == null) {
            logger.warn("ReactiveElasticsearchOperations is null, Elasticsearch features will be disabled");
            return;
        }
        
        if (surveyElasticsearchRepository == null || 
            questionElasticsearchRepository == null || 
            choiceElasticsearchRepository == null || 
            answerElasticsearchRepository == null) {
            logger.warn("One or more Elasticsearch repositories are null, Elasticsearch features will be disabled");
            return;
        }

        this.surveyElasticsearchRepository = surveyElasticsearchRepository;
        this.questionElasticsearchRepository = questionElasticsearchRepository;
        this.choiceElasticsearchRepository = choiceElasticsearchRepository;
        this.answerElasticsearchRepository = answerElasticsearchRepository;
        logger.info("Elasticsearch dependencies initialized");
    }
    
    public Flux<SearchResultDTO> searchAll(String query) {
        logger.info("Searching all entities with query: {}", query);
        elasticsearchDataIntegrity.validateSearchQuery(query);
        
        Flux<SearchResultDTO> surveyResults = searchSurveys(query)
            .map(survey -> createSearchResult("surveys", survey.getId(), "survey", survey));
        
        Flux<SearchResultDTO> questionResults = searchQuestions(query)
            .map(question -> createSearchResult("questions", question.getId(), "question", question));
        
        Flux<SearchResultDTO> choiceResults = searchChoices(query)
            .map(choice -> createSearchResult("choices", choice.getId(), "choice", choice));
        
        return Flux.concat(surveyResults, questionResults, choiceResults)
            .switchIfEmpty(Flux.just(createSearchResult("results", 0, "info", 
                Map.of("message", "No results found for query: " + query))))
            .doOnComplete(() -> logger.info("Search completed for query: {}", query))
            .onErrorResume(e -> {
                errorLogger.error("Error searching across all entities: {}", e.getMessage(), e);
                return Flux.empty();
            });
    }
    
    private SearchResultDTO createSearchResult(String index, Integer id, String type, Object content) {
        logger.debug("Creating search result: index={}, id={}, type={}", index, id, type);
        return SearchResultDTO.builder()
            .index(index)
            .id(id)
            .type(type)
            .content(content)
            .build();
    }
    
    public Flux<SurveyDocument> searchSurveys(String query) {
        logger.info("Searching surveys with query: {}", query);
        elasticsearchDataIntegrity.validateSearchQuery(query);
        
        if (surveyElasticsearchRepository != null) {
            logger.debug("Using Elasticsearch for survey search");
            // Use the new multi-word search method instead of the original one
            return surveyElasticsearchRepository.findByTitleOrDescriptionWithMultiWord(query)
                .switchIfEmpty(Flux.defer(() -> {
                    logger.info("No surveys found matching query: {}", query);
                    return Flux.empty();
                }))
                .doOnComplete(() -> logger.debug("Elasticsearch survey search completed for query: {}", query))
                .onErrorResume(e -> {
                    errorLogger.error("Error searching surveys in Elasticsearch: {}", e.getMessage(), e);
                    return fallbackToDbSurveySearch(query);
                });
        }

        logElastic();
        return fallbackToDbSurveySearch(query);
    }

    private static void logElastic() {
        logger.debug("Elasticsearch repository not available, using database search");
    }

    private Flux<SurveyDocument> fallbackToDbSurveySearch(String query) {
        logger.debug("Falling back to database search for surveys with query: {}", query);
        return surveyRepository.findAll()
            .filter(survey -> 
                (survey.getTitle() != null && survey.getTitle().toLowerCase().contains(query.toLowerCase())) || 
                (survey.getDescription() != null && survey.getDescription().toLowerCase().contains(query.toLowerCase())))
            .flatMap(survey -> questionRepository.findBySurveyId(survey.getId())
                .count()
                .map(count -> elasticsearchMapper.mapToSurveyDocument(survey, count.intValue())))
            .doOnComplete(() -> logger.debug("Database survey search completed for query: {}", query))
            .onErrorResume(e -> {
                errorLogger.error("Error in database fallback search for surveys: {}", e.getMessage(), e);
                return Flux.empty();
            });
    }
    
    public Flux<QuestionDocument> searchQuestions(String query) {
        logger.info("Searching questions with query: {}", query);
        elasticsearchDataIntegrity.validateSearchQuery(query);
        
        if (questionElasticsearchRepository != null) {
            logger.debug("Using Elasticsearch for question search");
            // Use the new multi-word search method
            return questionElasticsearchRepository.findByContentWithMultiWord(query)
                .switchIfEmpty(Flux.defer(() -> {
                    logger.info("No questions found matching query: {}", query);
                    return Flux.empty();
                }))
                .doOnComplete(() -> logger.debug("Elasticsearch question search completed for query: {}", query))
                .onErrorResume(e -> {
                    errorLogger.error("Error searching questions in Elasticsearch: {}", e.getMessage(), e);
                    return fallbackToDbQuestionSearch(query);
                });
        }

        logElastic();
        return fallbackToDbQuestionSearch(query);
    }
    
    private Flux<QuestionDocument> fallbackToDbQuestionSearch(String query) {
        logger.debug("Falling back to database search for questions with query: {}", query);
        return questionRepository.findAll()
            .filter(question -> 
                question.getContent() != null && 
                question.getContent().toLowerCase().contains(query.toLowerCase()))
            .map(elasticsearchMapper::mapToQuestionDocument)
            .doOnComplete(() -> logger.debug("Database question search completed for query: {}", query))
            .onErrorResume(e -> {
                errorLogger.error("Error in database fallback search for questions: {}", e.getMessage(), e);
                return Flux.empty();
            });
    }
    
    public Flux<ChoiceDocument> searchChoices(String query) {
        logger.info("Searching choices with query: {}", query);
        elasticsearchDataIntegrity.validateSearchQuery(query);
        
        if (choiceElasticsearchRepository != null) {
            logger.debug("Using Elasticsearch for choice search");
            // Use the new multi-word search method
            return choiceElasticsearchRepository.findByChoiceTextWithMultiWord(query)
                .switchIfEmpty(Flux.defer(() -> {
                    logger.info("No choices found matching query: {}", query);
                    return Flux.empty();
                }))
                .doOnComplete(() -> logger.debug("Elasticsearch choice search completed for query: {}", query))
                .onErrorResume(e -> {
                    errorLogger.error("Error searching choices in Elasticsearch: {}", e.getMessage(), e);
                    return fallbackToDbChoiceSearch(query);
                });
        }

        logElastic();
        return fallbackToDbChoiceSearch(query);
    }
    
    private Flux<ChoiceDocument> fallbackToDbChoiceSearch(String query) {
        logger.debug("Falling back to database search for choices with query: {}", query);
        return choiceRepository.findAll()
            .filter(choice -> 
                choice.getChoiceText() != null && 
                choice.getChoiceText().toLowerCase().contains(query.toLowerCase()))
            .map(elasticsearchMapper::mapToChoiceDocument)
            .doOnComplete(() -> logger.debug("Database choice search completed for query: {}", query))
            .onErrorResume(e -> {
                errorLogger.error("Error in database fallback search for choices: {}", e.getMessage(), e);
                return Flux.empty();
            });
    }
    
    public Flux<AnswerDocument> searchAnswersByQuestionId(Integer questionId) {
        logger.info("Searching answers for question ID: {}", questionId);
        elasticsearchDataIntegrity.validateQuestionId(questionId);
        
        if (answerElasticsearchRepository != null) {
            logger.debug("Using Elasticsearch for answer search by question ID: {}", questionId);
            return answerElasticsearchRepository.findByQuestionId(questionId)
                .switchIfEmpty(Flux.defer(() -> {
                    logger.info("No answers found for question ID: {}", questionId);
                    return Flux.empty();
                }))
                .doOnComplete(() -> logger.debug("Elasticsearch answer search completed for question ID: {}", questionId))
                .onErrorResume(e -> {
                    errorLogger.error("Error searching answers in Elasticsearch: {}", e.getMessage(), e);
                    return fallbackToDbAnswerSearch(answer -> answer.getQuestionId().equals(questionId));
                });
        }

        logElastic();
        return fallbackToDbAnswerSearch(answer -> answer.getQuestionId().equals(questionId));
    }
    
    public Mono<StatisticsDTO> getStatistics() {
        Mono<Long> totalSurveys = surveyRepository.count();
        Mono<Long> totalQuestions = questionRepository.count();
        Mono<Long> totalChoices = choiceRepository.count();
        Mono<Long> totalAnswers = answerRepository.count();
        Mono<Long> totalUsers = userRepository.count();
        Mono<Map<String, Long>> questionTypeStats = getQuestionTypeStatistics();
        
        return Mono.zip(totalSurveys, totalQuestions, totalChoices, totalAnswers, totalUsers, questionTypeStats)
                .map(tuple -> StatisticsDTO.builder()
                        .totalSurveys(tuple.getT1())
                        .totalQuestions(tuple.getT2())
                        .totalChoices(tuple.getT3())
                        .totalAnswers(tuple.getT4())
                        .totalUsers(tuple.getT5())
                        .questionTypeStats(tuple.getT6())
                        .build())
                .onErrorResume(e -> {
                    errorLogger.error("Error getting statistics: {}", e.getMessage(), e);
                    return Mono.just(new StatisticsDTO());
                });
    }
    
    public Mono<Map<String, Long>> getQuestionTypeStatistics() {
        return questionRepository.findAll()
                .groupBy(Question::getQuestionType)
                .flatMap(group -> Mono.just(group.key()).zipWith(group.count()))
                .collectMap(Tuple2::getT1, Tuple2::getT2)
                .onErrorReturn(Collections.emptyMap());
    }
    
    public Flux<Map<String, Object>> getUserParticipationStatistics() {
        return answerRepository.findAll()
                .groupBy(Answer::getUserId)
                .flatMap(group -> Mono.just(group.key()).zipWith(group.count()))
                .flatMap(tuple -> userRepository.findById(tuple.getT1())
                        .map(user -> {
                            Map<String, Object> result = new HashMap<>();
                            result.put("userId", tuple.getT1());
                            result.put("username", user.getUsername());
                            result.put("answerCount", tuple.getT2());
                            return result;
                        }))
                .onErrorResume(e -> {
                    errorLogger.error("Error getting user participation statistics: {}", e.getMessage(), e);
                    return Flux.empty();
                });
    }

    public Flux<AnswerDocument> searchAnswersByUserId(Integer userId) {
        logger.info("Searching answers for user ID: {}", userId);
        elasticsearchDataIntegrity.validateUserId(userId);
        
        if (answerElasticsearchRepository != null) {
            logger.debug("Using Elasticsearch for user answer search by user ID: {}", userId);
            return answerElasticsearchRepository.findByUserId(userId)
                .switchIfEmpty(Flux.defer(() -> {
                    logger.info("No answers found for user ID: {}", userId);
                    return Flux.empty();
                }))
                .doOnComplete(() -> logger.debug("Elasticsearch user answer search completed for user ID: {}", userId))
                .onErrorResume(e -> {
                    errorLogger.error("Error searching answers by user ID in Elasticsearch: {}", e.getMessage(), e);
                    return fallbackToDbAnswerSearch(answer -> answer.getUserId().equals(userId));
                });
        }

        logElastic();
        return fallbackToDbAnswerSearch(answer -> answer.getUserId().equals(userId));
    }

    public Flux<AnswerDocument> searchPublicAnswers() {
        logger.info("Searching for public answers");
        elasticsearchDataIntegrity.validatePublicFlag(true);
        
        if (answerElasticsearchRepository != null) {
            logger.debug("Using Elasticsearch for public answer search");
            return answerElasticsearchRepository.findByIsPublic(true)
                .switchIfEmpty(Flux.defer(() -> {
                    logger.info("No public answers found");
                    return Flux.empty();
                }))
                .doOnComplete(() -> logger.debug("Elasticsearch public answer search completed"))
                .onErrorResume(e -> {
                    errorLogger.error("Error searching public answers in Elasticsearch: {}", e.getMessage(), e);
                    return fallbackToDbAnswerSearch(answer -> answer.getIsPublic() != null && answer.getIsPublic());
                });
        }

        logElastic();
        return fallbackToDbAnswerSearch(answer -> answer.getIsPublic() != null && answer.getIsPublic());
    }

    public Flux<AnswerDocument> searchAnswersByQuestionIdAndUserId(Integer questionId, Integer userId) {
        logger.info("Searching answers for question ID: {} and user ID: {}", questionId, userId);
        elasticsearchDataIntegrity.validateQuestionId(questionId);
        elasticsearchDataIntegrity.validateUserId(userId);
        
        if (answerElasticsearchRepository != null) {
            logger.debug("Using Elasticsearch for answers by question and user search by question ID: {} and user ID: {}", questionId, userId);
            return answerElasticsearchRepository.findByQuestionIdAndUserId(questionId, userId)
                .switchIfEmpty(Flux.defer(() -> {
                    logger.info("No answers found for question ID: {} and user ID: {}", questionId, userId);
                    return Flux.empty();
                }))
                .doOnComplete(() -> logger.debug("Elasticsearch answers by question and user search completed for question ID: {} and user ID: {}", questionId, userId))
                .onErrorResume(e -> {
                    errorLogger.error("Error searching answers by question ID and user ID in Elasticsearch: {}", e.getMessage(), e);
                    return fallbackToDbAnswerSearch(answer -> 
                        answer.getQuestionId().equals(questionId) && answer.getUserId().equals(userId));
                });
        }

        logElastic();
        return fallbackToDbAnswerSearch(answer -> 
            answer.getQuestionId().equals(questionId) && answer.getUserId().equals(userId));
    }

    public Flux<QuestionDocument> searchQuestionsBySurveyId(Integer surveyId) {
        logger.info("Searching questions for survey ID: {}", surveyId);
        elasticsearchDataIntegrity.validateSurveyId(surveyId);
        
        if (questionElasticsearchRepository != null) {
            logger.debug("Using Elasticsearch for questions by survey search by survey ID: {}", surveyId);
            return questionElasticsearchRepository.findBySurveyId(surveyId)
                .switchIfEmpty(Flux.defer(() -> {
                    logger.info("No questions found for survey ID: {}", surveyId);
                    return Flux.empty();
                }))
                .doOnComplete(() -> logger.debug("Elasticsearch questions by survey search completed for survey ID: {}", surveyId))
                .onErrorResume(e -> {
                    errorLogger.error("Error searching questions by survey ID in Elasticsearch: {}", e.getMessage(), e);
                    return questionRepository.findBySurveyId(surveyId)
                        .map(elasticsearchMapper::mapToQuestionDocument)
                        .doOnComplete(() -> getDebug(surveyId))
                        .onErrorResume(ex -> {
                            errorLogger.error("Error in database search for questions by survey ID: {}", ex.getMessage(), ex);
                            return Flux.empty();
                        });
                });
        }

        logElastic();
        return questionRepository.findBySurveyId(surveyId)
            .map(elasticsearchMapper::mapToQuestionDocument)
            .doOnComplete(() -> getDebug(surveyId))
            .onErrorResume(e -> {
                errorLogger.error("Error in database search for questions by survey ID: {}", e.getMessage(), e);
                return Flux.empty();
            });
    }

    private static void getDebug(Integer surveyId) {
        logger.debug("Database questions by survey search completed for survey ID: {}", surveyId);
    }

    public Flux<QuestionDocument> searchQuestionsByType(String questionType) {
        logger.info("Searching questions of type: {}", questionType);
        elasticsearchDataIntegrity.validateQuestionType(questionType);
        
        if (questionElasticsearchRepository != null) {
            logger.debug("Using Elasticsearch for questions by type search by question type: {}", questionType);
            return questionElasticsearchRepository.findByQuestionType(questionType)
                .switchIfEmpty(Flux.defer(() -> {
                    logger.info("No questions found of type: {}", questionType);
                    return Flux.empty();
                }))
                .doOnComplete(() -> logger.debug("Elasticsearch questions by type search completed for question type: {}", questionType))
                .onErrorResume(e -> {
                    errorLogger.error("Error searching questions by type in Elasticsearch: {}", e.getMessage(), e);
                    return fallbackToDbQuestionSearchByType(questionType);
                });
        }

        logElastic();
        return fallbackToDbQuestionSearchByType(questionType);
    }

    private Flux<QuestionDocument> fallbackToDbQuestionSearchByType(String questionType) {
        logger.debug("Falling back to database search for questions by type with query: {}", questionType);
        return questionRepository.findAll()
            .filter(question -> question.getQuestionType() != null && 
                    question.getQuestionType().equalsIgnoreCase(questionType))
            .map(elasticsearchMapper::mapToQuestionDocument)
            .doOnComplete(() -> logger.debug("Database questions by type search completed for question type: {}", questionType))
            .onErrorResume(e -> {
                errorLogger.error("Error in database fallback search for questions by type: {}", e.getMessage(), e);
                return Flux.empty();
            });
    }

    public Flux<ChoiceDocument> searchChoicesByQuestionId(Integer questionId) {
        logger.info("Searching choices for question ID: {}", questionId);
        elasticsearchDataIntegrity.validateQuestionId(questionId);
        
        if (choiceElasticsearchRepository != null) {
            logger.debug("Using Elasticsearch for choices by question search by question ID: {}", questionId);
            return choiceElasticsearchRepository.findByQuestionId(questionId)
                .switchIfEmpty(Flux.defer(() -> {
                    logger.info("No choices found for question ID: {}", questionId);
                    return Flux.empty();
                }))
                .doOnComplete(() -> logger.debug("Elasticsearch choices by question search completed for question ID: {}", questionId))
                .onErrorResume(e -> {
                    errorLogger.error("Error searching choices by question ID in Elasticsearch: {}", e.getMessage(), e);
                    return choiceRepository.findByQuestionId(questionId)
                        .map(elasticsearchMapper::mapToChoiceDocument)
                        .doOnComplete(() -> logQuestionId(questionId))
                        .onErrorResume(ex -> {
                            errorLogger.error("Error in database search for choices by question ID: {}", ex.getMessage(), ex);
                            return Flux.empty();
                        });
                });
        }

        logElastic();
        return choiceRepository.findByQuestionId(questionId)
            .map(elasticsearchMapper::mapToChoiceDocument)
            .doOnComplete(() -> logQuestionId(questionId))
            .onErrorResume(e -> {
                errorLogger.error("Error in database search for choices by question ID: {}", e.getMessage(), e);
                return Flux.empty();
            });
    }

    private static void logQuestionId(Integer questionId) {
        logger.debug("Database choices by question search completed for question ID: {}", questionId);
    }

    private Flux<AnswerDocument> fallbackToDbAnswerSearch(Predicate<Answer> filter) {
        logger.debug("Falling back to database search for answers");
        return answerRepository.findAll()
            .filter(filter)
            .map(elasticsearchMapper::mapToAnswerDocument)
            .doOnComplete(() -> logger.debug("Database answer search completed"))
            .onErrorResume(e -> {
                errorLogger.error("Error in database fallback search for answers: {}", e.getMessage(), e);
                return Flux.empty();
            });
    }
}