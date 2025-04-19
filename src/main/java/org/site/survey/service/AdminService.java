package org.site.survey.service;

import lombok.extern.slf4j.Slf4j;
import org.site.survey.dto.StatisticsDTO;
import org.site.survey.dto.response.SearchResultDTO;
import org.site.survey.integrity.ElasticsearchDataIntegrity;
import org.site.survey.model.Answer;
import org.site.survey.model.Choice;
import org.site.survey.model.Question;
import org.site.survey.model.Survey;
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
@Slf4j
public class AdminService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ElasticsearchDataIntegrity elasticsearchDataIntegrity;

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
            ElasticsearchDataIntegrity elasticsearchDataIntegrity) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.elasticsearchDataIntegrity = elasticsearchDataIntegrity;
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
            log.warn("ReactiveElasticsearchOperations is null, Elasticsearch features will be disabled");
            return;
        }
        
        if (surveyElasticsearchRepository == null || 
            questionElasticsearchRepository == null || 
            choiceElasticsearchRepository == null || 
            answerElasticsearchRepository == null) {
            log.warn("One or more Elasticsearch repositories are null, Elasticsearch features will be disabled");
            return;
        }

        this.surveyElasticsearchRepository = surveyElasticsearchRepository;
        this.questionElasticsearchRepository = questionElasticsearchRepository;
        this.choiceElasticsearchRepository = choiceElasticsearchRepository;
        this.answerElasticsearchRepository = answerElasticsearchRepository;
        log.info("Elasticsearch dependencies initialized");
    }
    
    public Flux<SearchResultDTO> searchAll(String query) {
        log.info("Searching all entities with query: {}", query);
        elasticsearchDataIntegrity.validateSearchQuery(query);
        
        Flux<SearchResultDTO> surveyResults = searchSurveys(query)
            .map(survey -> createSearchResult("surveys", survey.getId().toString(), "survey", survey));
        
        Flux<SearchResultDTO> questionResults = searchQuestions(query)
            .map(question -> createSearchResult("questions", question.getId().toString(), "question", question));
        
        Flux<SearchResultDTO> choiceResults = searchChoices(query)
            .map(choice -> createSearchResult("choices", choice.getId().toString(), "choice", choice));
        
        return Flux.concat(surveyResults, questionResults, choiceResults)
            .switchIfEmpty(Flux.just(createSearchResult("results", "0", "info", 
                Map.of("message", "No results found for query: " + query))))
            .onErrorResume(e -> {
                log.error("Error searching across all entities", e);
                return Flux.empty();
            });
    }
    
    private SearchResultDTO createSearchResult(String index, String id, String type, Object content) {
        return SearchResultDTO.builder()
            .index(index)
            .id(id)
            .type(type)
            .content(content)
            .build();
    }
    
    public Flux<SurveyDocument> searchSurveys(String query) {
        log.info("Searching surveys with query: {}", query);
        elasticsearchDataIntegrity.validateSearchQuery(query);
        
        if (surveyElasticsearchRepository != null) {
            log.debug("Using Elasticsearch for survey search");
            return surveyElasticsearchRepository.findByTitleContainingOrDescriptionContaining(query, query)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("No surveys found matching query: {}", query);
                    return Flux.empty();
                }))
                .onErrorResume(e -> {
                    log.error("Error searching surveys in Elasticsearch", e);
                    return fallbackToDbSurveySearch(query);
                });
        }
        
        return fallbackToDbSurveySearch(query);
    }
    
    private Flux<SurveyDocument> fallbackToDbSurveySearch(String query) {
        log.debug("Falling back to database search for surveys");
        return surveyRepository.findAll()
            .filter(survey -> 
                (survey.getTitle() != null && survey.getTitle().toLowerCase().contains(query.toLowerCase())) || 
                (survey.getDescription() != null && survey.getDescription().toLowerCase().contains(query.toLowerCase())))
            .flatMap(survey -> questionRepository.findBySurveyId(survey.getId())
                .count()
                .map(count -> mapToSurveyDocument(survey, count.intValue())))
            .onErrorResume(e -> {
                log.error("Error in database fallback search for surveys", e);
                return Flux.empty();
            });
    }
    
    private SurveyDocument mapToSurveyDocument(Survey survey, int questionCount) {
        SurveyDocument doc = new SurveyDocument();
        doc.setId(survey.getId());
        doc.setTitle(survey.getTitle());
        doc.setDescription(survey.getDescription());
        doc.setQuestionSize(questionCount);
        doc.setCreatedBy(survey.getCreatedBy());
        doc.setCreatedAt(survey.getCreatedAt());
        return doc;
    }
    
    public Flux<QuestionDocument> searchQuestions(String query) {
        log.info("Searching questions with query: {}", query);
        elasticsearchDataIntegrity.validateSearchQuery(query);
        
        if (questionElasticsearchRepository != null) {
            log.debug("Using Elasticsearch for question search");
            return questionElasticsearchRepository.findByContentContaining(query)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("No questions found matching query: {}", query);
                    return Flux.empty();
                }))
                .onErrorResume(e -> {
                    log.error("Error searching questions in Elasticsearch", e);
                    return fallbackToDbQuestionSearch(query);
                });
        }
        
        return fallbackToDbQuestionSearch(query);
    }
    
    private Flux<QuestionDocument> fallbackToDbQuestionSearch(String query) {
        log.debug("Falling back to database search for questions");
        return questionRepository.findAll()
            .filter(question -> 
                question.getContent() != null && 
                question.getContent().toLowerCase().contains(query.toLowerCase()))
            .map(this::mapToQuestionDocument)
            .onErrorResume(e -> {
                log.error("Error in database fallback search for questions", e);
                return Flux.empty();
            });
    }
    
    private QuestionDocument mapToQuestionDocument(Question question) {
        QuestionDocument doc = new QuestionDocument();
        doc.setId(question.getId());
        doc.setSurveyId(question.getSurveyId());
        doc.setContent(question.getContent());
        doc.setQuestionType(question.getQuestionType());
        doc.setCreatedAt(question.getCreatedAt());
        return doc;
    }
    
    public Flux<ChoiceDocument> searchChoices(String query) {
        log.info("Searching choices with query: {}", query);
        elasticsearchDataIntegrity.validateSearchQuery(query);
        
        if (choiceElasticsearchRepository != null) {
            log.debug("Using Elasticsearch for choice search");
            return choiceElasticsearchRepository.findByChoiceTextContaining(query)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("No choices found matching query: {}", query);
                    return Flux.empty();
                }))
                .onErrorResume(e -> {
                    log.error("Error searching choices in Elasticsearch", e);
                    return fallbackToDbChoiceSearch(query);
                });
        }
        
        return fallbackToDbChoiceSearch(query);
    }
    
    private Flux<ChoiceDocument> fallbackToDbChoiceSearch(String query) {
        log.debug("Falling back to database search for choices");
        return choiceRepository.findAll()
            .filter(choice -> 
                choice.getChoiceText() != null && 
                choice.getChoiceText().toLowerCase().contains(query.toLowerCase()))
            .map(this::mapToChoiceDocument)
            .onErrorResume(e -> {
                log.error("Error in database fallback search for choices", e);
                return Flux.empty();
            });
    }
    
    private ChoiceDocument mapToChoiceDocument(Choice choice) {
        ChoiceDocument doc = new ChoiceDocument();
        doc.setId(choice.getId());
        doc.setQuestionId(choice.getQuestionId());
        doc.setChoiceText(choice.getChoiceText());
        return doc;
    }
    
    public Flux<AnswerDocument> searchAnswersByQuestionId(Integer questionId) {
        log.info("Searching answers for question ID: {}", questionId);
        elasticsearchDataIntegrity.validateQuestionId(questionId);
        
        if (answerElasticsearchRepository != null) {
            log.debug("Using Elasticsearch for answer search");
            return answerElasticsearchRepository.findByQuestionId(questionId)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("No answers found for question ID: {}", questionId);
                    return Flux.empty();
                }))
                .onErrorResume(e -> {
                    log.error("Error searching answers in Elasticsearch", e);
                    return fallbackToDbAnswerSearch(answer -> answer.getQuestionId().equals(questionId));
                });
        }
        
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
                    log.error("Error getting statistics", e);
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
                    log.error("Error getting user participation statistics", e);
                    return Flux.empty();
                });
    }

    public Flux<AnswerDocument> searchAnswersByUserId(Integer userId) {
        log.info("Searching answers for user ID: {}", userId);
        elasticsearchDataIntegrity.validateUserId(userId);
        
        if (answerElasticsearchRepository != null) {
            log.debug("Using Elasticsearch for user answer search");
            return answerElasticsearchRepository.findByUserId(userId)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("No answers found for user ID: {}", userId);
                    return Flux.empty();
                }))
                .onErrorResume(e -> {
                    log.error("Error searching answers by user ID in Elasticsearch", e);
                    return fallbackToDbAnswerSearch(answer -> answer.getUserId().equals(userId));
                });
        }
        
        return fallbackToDbAnswerSearch(answer -> answer.getUserId().equals(userId));
    }

    public Flux<AnswerDocument> searchPublicAnswers() {
        log.info("Searching for public answers");
        elasticsearchDataIntegrity.validatePublicFlag(true);
        
        if (answerElasticsearchRepository != null) {
            log.debug("Using Elasticsearch for public answer search");
            return answerElasticsearchRepository.findByIsPublic(true)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("No public answers found");
                    return Flux.empty();
                }))
                .onErrorResume(e -> {
                    log.error("Error searching public answers in Elasticsearch", e);
                    return fallbackToDbAnswerSearch(answer -> answer.getIsPublic() != null && answer.getIsPublic());
                });
        }
        
        return fallbackToDbAnswerSearch(answer -> answer.getIsPublic() != null && answer.getIsPublic());
    }

    public Flux<AnswerDocument> searchAnswersByQuestionIdAndUserId(Integer questionId, Integer userId) {
        log.info("Searching answers for question ID: {} and user ID: {}", questionId, userId);
        elasticsearchDataIntegrity.validateQuestionId(questionId);
        elasticsearchDataIntegrity.validateUserId(userId);
        
        if (answerElasticsearchRepository != null) {
            log.debug("Using Elasticsearch for answers by question and user search");
            return answerElasticsearchRepository.findByQuestionIdAndUserId(questionId, userId)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("No answers found for question ID: {} and user ID: {}", questionId, userId);
                    return Flux.empty();
                }))
                .onErrorResume(e -> {
                    log.error("Error searching answers by question ID and user ID in Elasticsearch", e);
                    return fallbackToDbAnswerSearch(answer -> 
                        answer.getQuestionId().equals(questionId) && answer.getUserId().equals(userId));
                });
        }
        
        return fallbackToDbAnswerSearch(answer -> 
            answer.getQuestionId().equals(questionId) && answer.getUserId().equals(userId));
    }

    public Flux<QuestionDocument> searchQuestionsBySurveyId(Integer surveyId) {
        log.info("Searching questions for survey ID: {}", surveyId);
        elasticsearchDataIntegrity.validateSurveyId(surveyId);
        
        if (questionElasticsearchRepository != null) {
            log.debug("Using Elasticsearch for questions by survey search");
            return questionElasticsearchRepository.findBySurveyId(surveyId)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("No questions found for survey ID: {}", surveyId);
                    return Flux.empty();
                }))
                .onErrorResume(e -> {
                    log.error("Error searching questions by survey ID in Elasticsearch", e);
                    return questionRepository.findBySurveyId(surveyId)
                        .map(this::mapToQuestionDocument)
                        .onErrorResume(ex -> {
                            log.error("Error in database search for questions by survey ID", ex);
                            return Flux.empty();
                        });
                });
        }
        
        return questionRepository.findBySurveyId(surveyId)
            .map(this::mapToQuestionDocument)
            .onErrorResume(e -> {
                log.error("Error in database search for questions by survey ID", e);
                return Flux.empty();
            });
    }

    public Flux<QuestionDocument> searchQuestionsByType(String questionType) {
        log.info("Searching questions of type: {}", questionType);
        elasticsearchDataIntegrity.validateQuestionType(questionType);
        
        if (questionElasticsearchRepository != null) {
            log.debug("Using Elasticsearch for questions by type search");
            return questionElasticsearchRepository.findByQuestionType(questionType)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("No questions found of type: {}", questionType);
                    return Flux.empty();
                }))
                .onErrorResume(e -> {
                    log.error("Error searching questions by type in Elasticsearch", e);
                    return fallbackToDbQuestionSearchByType(questionType);
                });
        }
        
        return fallbackToDbQuestionSearchByType(questionType);
    }

    private Flux<QuestionDocument> fallbackToDbQuestionSearchByType(String questionType) {
        log.debug("Falling back to database search for questions by type");
        return questionRepository.findAll()
            .filter(question -> question.getQuestionType() != null && 
                    question.getQuestionType().equalsIgnoreCase(questionType))
            .map(this::mapToQuestionDocument)
            .onErrorResume(e -> {
                log.error("Error in database fallback search for questions by type", e);
                return Flux.empty();
            });
    }

    public Flux<ChoiceDocument> searchChoicesByQuestionId(Integer questionId) {
        log.info("Searching choices for question ID: {}", questionId);
        elasticsearchDataIntegrity.validateQuestionId(questionId);
        
        if (choiceElasticsearchRepository != null) {
            log.debug("Using Elasticsearch for choices by question search");
            return choiceElasticsearchRepository.findByQuestionId(questionId)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("No choices found for question ID: {}", questionId);
                    return Flux.empty();
                }))
                .onErrorResume(e -> {
                    log.error("Error searching choices by question ID in Elasticsearch", e);
                    return choiceRepository.findByQuestionId(questionId)
                        .map(this::mapToChoiceDocument)
                        .onErrorResume(ex -> {
                            log.error("Error in database search for choices by question ID", ex);
                            return Flux.empty();
                        });
                });
        }
        
        return choiceRepository.findByQuestionId(questionId)
            .map(this::mapToChoiceDocument)
            .onErrorResume(e -> {
                log.error("Error in database search for choices by question ID", e);
                return Flux.empty();
            });
    }

    private Flux<AnswerDocument> fallbackToDbAnswerSearch(Predicate<Answer> filter) {
        log.debug("Falling back to database search for answers");
        return answerRepository.findAll()
            .filter(filter)
            .map(this::mapToAnswerDocument)
            .onErrorResume(e -> {
                log.error("Error in database fallback search for answers", e);
                return Flux.empty();
            });
    }
    
    private AnswerDocument mapToAnswerDocument(Answer answer) {
        AnswerDocument doc = new AnswerDocument();
        doc.setId(answer.getId());
        doc.setQuestionId(answer.getQuestionId());
        doc.setUserId(answer.getUserId());
        doc.setChoiceId(answer.getChoiceId());
        doc.setIsPublic(answer.getIsPublic());
        doc.setCreatedAt(answer.getCreatedAt());
        return doc;
    }
} 