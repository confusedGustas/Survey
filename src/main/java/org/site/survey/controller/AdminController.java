package org.site.survey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.site.survey.dto.StatisticsDTO;
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
@Tag(name = "Admin Management", description = "APIs for administrative operations and data management")
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
    @Operation(
        summary = "Search across all entities",
        description = "Searches for the query string in surveys, questions, and choices"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchAll(
            @Parameter(description = "Search query string", required = true) 
            @RequestParam String query) {
        return ResponseUtils.wrapFluxResponse(adminService.searchAll(query), "search results");
    }

    @GetMapping("/search/surveys")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search surveys",
        description = "Searches for the query string in survey titles and descriptions"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchSurveys(
            @Parameter(description = "Search query string", required = true)
            @RequestParam String query) {
        return ResponseUtils.wrapFluxResponse(adminService.searchSurveys(query), "surveys");
    }

    @GetMapping("/search/questions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search questions",
        description = "Searches for the query string in question content"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchQuestions(
            @Parameter(description = "Search query string", required = true)
            @RequestParam String query) {
        return ResponseUtils.wrapFluxResponse(adminService.searchQuestions(query), "questions");
    }
    
    @GetMapping("/search/questions/survey")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search questions by survey ID",
        description = "Retrieves all questions belonging to a specific survey"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid survey ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchQuestionsBySurveyId(
            @Parameter(description = "Survey ID", required = true, example = "1")
            @RequestParam Integer surveyId) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchQuestionsBySurveyId(surveyId), 
            "questions for survey " + surveyId
        );
    }
    
    @GetMapping("/search/questions/type")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search questions by type",
        description = "Retrieves all questions of a specific question type"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid question type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchQuestionsByType(
            @Parameter(description = "Question type", required = true, example = "MULTIPLE_CHOICE")
            @RequestParam String type) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchQuestionsByType(type), 
            "questions of type " + type
        );
    }

    @GetMapping("/search/choices")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search choices",
        description = "Searches for the query string in choice text"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchChoices(
            @Parameter(description = "Search query string", required = true)
            @RequestParam String query) {
        return ResponseUtils.wrapFluxResponse(adminService.searchChoices(query), "choices");
    }
    
    @GetMapping("/search/choices/question")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search choices by question ID",
        description = "Retrieves all choices belonging to a specific question"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid question ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchChoicesByQuestionId(
            @Parameter(description = "Question ID", required = true, example = "1")
            @RequestParam Integer questionId) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchChoicesByQuestionId(questionId), 
            "choices for question " + questionId
        );
    }

    @GetMapping("/search/answers/question")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search answers by question ID",
        description = "Retrieves all answers for a specific question"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid question ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchAnswersByQuestionId(
            @Parameter(description = "Question ID", required = true, example = "1")
            @RequestParam Integer questionId) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchAnswersByQuestionId(questionId), 
            "answers for question " + questionId
        );
    }
    
    @GetMapping("/search/answers/user")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search answers by user ID",
        description = "Retrieves all answers submitted by a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchAnswersByUserId(
            @Parameter(description = "User ID", required = true, example = "1")
            @RequestParam Integer userId) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchAnswersByUserId(userId), 
            "answers for user " + userId
        );
    }
    
    @GetMapping("/search/answers/public")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search public answers",
        description = "Retrieves all answers marked as public"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchPublicAnswers() {
        return ResponseUtils.wrapFluxResponse(adminService.searchPublicAnswers(), "public answers");
    }
    
    @GetMapping("/search/answers/question-user")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search answers by question ID and user ID",
        description = "Retrieves all answers for a specific question submitted by a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid question ID or user ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchAnswersByQuestionIdAndUserId(
            @Parameter(description = "Question ID", required = true, example = "1")
            @RequestParam Integer questionId, 
            @Parameter(description = "User ID", required = true, example = "1")
            @RequestParam Integer userId) {
        return ResponseUtils.wrapFluxResponse(
            adminService.searchAnswersByQuestionIdAndUserId(questionId, userId), 
            "answers for question " + questionId + " and user " + userId
        );
    }
    
    @PostMapping("/elasticsearch/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Synchronize Elasticsearch",
        description = "Synchronizes all data between the database and Elasticsearch"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Synchronization completed successfully or Elasticsearch is disabled"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error during synchronization")
    })
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
    @Operation(
        summary = "Get system statistics",
        description = "Retrieves statistics about surveys, questions, choices, answers, and users"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = StatisticsDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
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
    @Operation(
        summary = "Get question type statistics",
        description = "Retrieves statistics about question types and their distribution"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
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
    @Operation(
        summary = "Get user participation statistics",
        description = "Retrieves statistics about user participation and answer counts"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> getUserParticipationStatistics() {
        return ResponseUtils.wrapFluxResponse(
            adminService.getUserParticipationStatistics(), 
            "user participation statistics"
        );
    }
} 