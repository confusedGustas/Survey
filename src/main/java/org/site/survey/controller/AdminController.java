package org.site.survey.controller;

import org.site.survey.service.AdminService;
import org.site.survey.service.ElasticsearchSyncService;
import org.site.survey.util.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ElasticsearchSyncService elasticsearchSyncService;

    @Autowired
    public AdminController(AdminService adminService, 
                           @Autowired(required = false) ElasticsearchSyncService elasticsearchSyncService) {
        this.adminService = adminService;
        this.elasticsearchSyncService = elasticsearchSyncService;
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchAll(@RequestParam String query) {
        return ResponseUtils.wrapFluxResponse(adminService.searchAll(query), "search results");
    }

    @GetMapping("/search/surveys")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchSurveys(@RequestParam String query) {
        return ResponseUtils.wrapFluxResponse(adminService.searchSurveys(query), "surveys");
    }

    @GetMapping("/search/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchQuestions(@RequestParam String query) {
        return ResponseUtils.wrapFluxResponse(adminService.searchQuestions(query), "questions");
    }
    
    @GetMapping("/search/questions/survey")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchQuestionsBySurveyId(@RequestParam Integer surveyId) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchQuestionsBySurveyId(surveyId), 
            "questions for survey " + surveyId
        );
    }
    
    @GetMapping("/search/questions/type")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchQuestionsByType(@RequestParam String type) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchQuestionsByType(type), 
            "questions of type " + type
        );
    }

    @GetMapping("/search/choices")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchChoices(@RequestParam String query) {
        return ResponseUtils.wrapFluxResponse(adminService.searchChoices(query), "choices");
    }
    
    @GetMapping("/search/choices/question")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchChoicesByQuestionId(@RequestParam Integer questionId) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchChoicesByQuestionId(questionId), 
            "choices for question " + questionId
        );
    }

    @GetMapping("/search/answers/question")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchAnswersByQuestionId(@RequestParam Integer questionId) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchAnswersByQuestionId(questionId), 
            "answers for question " + questionId
        );
    }
    
    @GetMapping("/search/answers/user")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchAnswersByUserId(@RequestParam Integer userId) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchAnswersByUserId(userId), 
            "answers for user " + userId
        );
    }
    
    @GetMapping("/search/answers/public")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchPublicAnswers() {
        return ResponseUtils.wrapFluxResponse(adminService.searchPublicAnswers(), "public answers");
    }
    
    @GetMapping("/search/answers/question-user")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> searchAnswersByQuestionIdAndUserId(
            @RequestParam Integer questionId, 
            @RequestParam Integer userId) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchAnswersByQuestionIdAndUserId(questionId, userId), 
            "answers for question " + questionId + " and user " + userId
        );
    }
    
    @PostMapping("/elasticsearch/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Map<String, String>>> syncElasticsearch() {
        if (elasticsearchSyncService == null) {
            return Mono.just(ResponseEntity.ok(Map.of("status", "Elasticsearch is not enabled")));
        }
        
        return elasticsearchSyncService.syncAllData()
            .then(Mono.just(ResponseEntity.ok(Map.of("status", "Elasticsearch sync completed successfully"))))
            .onErrorResume(e -> Mono.just(ResponseEntity.ok(Map.of(
                "status", "Failed to sync with Elasticsearch",
                "error", e.getMessage()
            ))));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> getStatistics() {
        return adminService.getStatistics()
            .map(stats -> {
                if (stats.getTotalSurveys() == 0 && stats.getTotalQuestions() == 0 && 
                    stats.getTotalChoices() == 0 && stats.getTotalAnswers() == 0) {
                    return ResponseEntity.ok(ResponseUtils.emptyResponseMessage("statistics"));
                } else {
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "data", stats
                    );
                    return ResponseEntity.ok(response);
                }
            });
    }

    @GetMapping("/statistics/question-types")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> getQuestionTypeStatistics() {
        return adminService.getQuestionTypeStatistics()
            .map(stats -> {
                if (stats.isEmpty()) {
                    return ResponseEntity.ok(ResponseUtils.emptyResponseMessage("question type statistics"));
                } else {
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "data", stats
                    );
                    return ResponseEntity.ok(response);
                }
            });
    }

    @GetMapping("/statistics/user-participation")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Object>> getUserParticipationStatistics() {
        return ResponseUtils.wrapFluxResponse(
            adminService.getUserParticipationStatistics(), 
            "user participation statistics"
        );
    }
} 