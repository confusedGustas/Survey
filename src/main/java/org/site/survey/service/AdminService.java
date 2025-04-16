package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.site.survey.dto.StatisticsDTO;
import org.site.survey.model.Answer;
import org.site.survey.model.elasticsearch.*;
import org.site.survey.repository.*;
import org.site.survey.repository.elasticsearch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AdminService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    
    // Optional dependencies for Elasticsearch
    private ReactiveElasticsearchOperations operations;
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
            UserRepository userRepository) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
    }
    
    @Autowired(required = false)
    @ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
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
        
        // Check for null repositories
        if (surveyElasticsearchRepository == null || 
            questionElasticsearchRepository == null || 
            choiceElasticsearchRepository == null || 
            answerElasticsearchRepository == null) {
            log.warn("One or more Elasticsearch repositories are null, Elasticsearch features will be disabled");
            return;
        }
        
        this.operations = operations;
        this.surveyElasticsearchRepository = surveyElasticsearchRepository;
        this.questionElasticsearchRepository = questionElasticsearchRepository;
        this.choiceElasticsearchRepository = choiceElasticsearchRepository;
        this.answerElasticsearchRepository = answerElasticsearchRepository;
        log.info("Elasticsearch dependencies initialized");
    }
    
    // Search across all entities
    public Flux<org.site.survey.dto.response.SearchResultDTO> searchAll(String query) {
        log.info("Searching all entities with query: {}", query);
        
        // Create search results from surveys
        Flux<org.site.survey.dto.response.SearchResultDTO> surveyResults = searchSurveys(query)
            .map(survey -> org.site.survey.dto.response.SearchResultDTO.builder()
                .index("surveys")
                .id(survey.getId().toString())
                .type("survey")
                .content(survey)
                .build());
        
        // Create search results from questions
        Flux<org.site.survey.dto.response.SearchResultDTO> questionResults = searchQuestions(query)
            .map(question -> org.site.survey.dto.response.SearchResultDTO.builder()
                .index("questions")
                .id(question.getId().toString())
                .type("question")
                .content(question)
                .build());
        
        // Create search results from choices
        Flux<org.site.survey.dto.response.SearchResultDTO> choiceResults = searchChoices(query)
            .map(choice -> org.site.survey.dto.response.SearchResultDTO.builder()
                .index("choices")
                .id(choice.getId().toString())
                .type("choice")
                .content(choice)
                .build());
        
        // Combine all results
        return Flux.concat(surveyResults, questionResults, choiceResults)
            .onErrorResume(e -> {
                log.error("Error searching across all entities", e);
                return Flux.empty();
            });
    }
    
    // Search surveys
    public Flux<SurveyDocument> searchSurveys(String query) {
        log.info("Searching surveys with query: {}", query);
        return surveyRepository.findAll()
            .filter(survey -> 
                (survey.getTitle() != null && survey.getTitle().toLowerCase().contains(query.toLowerCase())) || 
                (survey.getDescription() != null && survey.getDescription().toLowerCase().contains(query.toLowerCase())))
            .flatMap(survey -> {
                // For each survey, find out how many questions it has for the questionSize
                return questionRepository.findBySurveyId(survey.getId())
                    .count()
                    .map(count -> {
                        SurveyDocument doc = new SurveyDocument();
                        doc.setId(survey.getId());
                        doc.setTitle(survey.getTitle());
                        doc.setDescription(survey.getDescription());
                        doc.setQuestionSize(count.intValue()); // Set the question count as the size
                        doc.setCreatedBy(survey.getCreatedBy());
                        doc.setCreatedAt(survey.getCreatedAt());
                        return doc;
                    });
            })
            .onErrorResume(e -> {
                log.error("Error searching surveys", e);
                return Flux.empty();
            });
    }
    
    // Search questions
    public Flux<QuestionDocument> searchQuestions(String query) {
        log.info("Searching questions with query: {}", query);
        return questionRepository.findAll()
            .filter(question -> 
                question.getContent() != null && 
                question.getContent().toLowerCase().contains(query.toLowerCase()))
            .map(question -> {
                QuestionDocument doc = new QuestionDocument();
                doc.setId(question.getId());
                doc.setSurveyId(question.getSurveyId());
                doc.setContent(question.getContent());
                doc.setQuestionType(question.getQuestionType());
                doc.setCreatedAt(question.getCreatedAt());
                return doc;
            })
            .onErrorResume(e -> {
                log.error("Error searching questions", e);
                return Flux.empty();
            });
    }
    
    // Search choices
    public Flux<ChoiceDocument> searchChoices(String query) {
        log.info("Searching choices with query: {}", query);
        return choiceRepository.findAll()
            .filter(choice -> 
                choice.getChoiceText() != null && 
                choice.getChoiceText().toLowerCase().contains(query.toLowerCase()))
            .map(choice -> {
                ChoiceDocument doc = new ChoiceDocument();
                doc.setId(choice.getId());
                doc.setQuestionId(choice.getQuestionId());
                doc.setChoiceText(choice.getChoiceText());
                return doc;
            })
            .onErrorResume(e -> {
                log.error("Error searching choices", e);
                return Flux.empty();
            });
    }
    
    // Search answers by question ID
    public Flux<AnswerDocument> searchAnswersByQuestionId(Integer questionId) {
        log.info("Searching answers for question ID: {}", questionId);
        return answerRepository.findAll()
            .filter(answer -> answer.getQuestionId().equals(questionId))
            .map(answer -> {
                AnswerDocument doc = new AnswerDocument();
                doc.setId(answer.getId());
                doc.setQuestionId(answer.getQuestionId());
                doc.setUserId(answer.getUserId());
                doc.setChoiceId(answer.getChoiceId());
                doc.setIsPublic(answer.getIsPublic());
                doc.setCreatedAt(answer.getCreatedAt());
                return doc;
            })
            .onErrorResume(e -> {
                log.error("Error searching answers by question ID", e);
                return Flux.empty();
            });
    }
    
    // Get statistics about the system
    public Mono<StatisticsDTO> getStatistics() {
        Mono<Long> totalSurveys = surveyRepository.count();
        Mono<Long> totalQuestions = questionRepository.count();
        Mono<Long> totalChoices = choiceRepository.count();
        Mono<Long> totalAnswers = answerRepository.count();
        Mono<Long> totalUsers = userRepository.count();
        
        // Get question type statistics for including in the response
        Mono<Map<String, Long>> questionTypeStats = getQuestionTypeStatistics();
        
        return Mono.zip(totalSurveys, totalQuestions, totalChoices, totalAnswers, totalUsers, questionTypeStats)
                .map(tuple -> {
                    return StatisticsDTO.builder()
                            .totalSurveys(tuple.getT1())
                            .totalQuestions(tuple.getT2())
                            .totalChoices(tuple.getT3())
                            .totalAnswers(tuple.getT4())
                            .totalUsers(tuple.getT5())
                            .questionTypeStats(tuple.getT6())
                            .build();
                })
                .onErrorResume(e -> {
                    log.error("Error getting statistics", e);
                    return Mono.just(new StatisticsDTO());
                });
    }
    
    // Get statistics about question types
    public Mono<Map<String, Long>> getQuestionTypeStatistics() {
        return questionRepository.findAll()
                .groupBy(question -> question.getQuestionType())
                .flatMap(group -> Mono.just(group.key()).zipWith(group.count()))
                .collectMap(tuple -> tuple.getT1(), tuple -> tuple.getT2())
                .onErrorReturn(Collections.emptyMap());
    }
    
    // Get statistics about users' participation
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
} 