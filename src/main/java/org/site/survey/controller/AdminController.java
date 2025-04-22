package org.site.survey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.Logger;
import org.site.survey.dto.StatisticsDTO;
import org.site.survey.service.AdminService;
import org.site.survey.service.ElasticsearchSyncService;
import org.site.survey.util.LoggerUtil;
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

    private static final Logger logger = LoggerUtil.getLogger(AdminController.class);
    private static final Logger errorLogger = LoggerUtil.getErrorLogger(AdminController.class);

    private final AdminService adminService;
    private final ElasticsearchSyncService elasticsearchSyncService;

    @Autowired
    public AdminController(AdminService adminService, 
                           @Autowired(required = false) ElasticsearchSyncService elasticsearchSyncService) {
        this.adminService = adminService;
        this.elasticsearchSyncService = elasticsearchSyncService;
        logger.info("AdminController initialized");
        logger.debug("Elasticsearch sync service available: {}", (elasticsearchSyncService != null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search across all entities",
        description = "Searches for the query string in surveys, questions, and choices with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchAll(
            @Parameter(description = "Search query string", required = true) 
            @RequestParam String query,
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Performing global search with query: '{}', page: {}, size: {}", query, page, size);
        return ResponseUtils.wrapFluxResponsePaginated(adminService.searchAll(query), "search results", page, size)
                .doOnSuccess(response -> logger.info("Global search completed successfully"))
                .doOnError(e -> errorLogger.error("Error during global search: {}", e.getMessage(), e));
    }

    @GetMapping("/search/surveys")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search surveys",
        description = "Searches for the query string in survey titles and descriptions with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchSurveys(
            @Parameter(description = "Search query string", required = true)
            @RequestParam String query,
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Searching surveys with query: '{}', page: {}, size: {}", query, page, size);
        return ResponseUtils.wrapFluxResponsePaginated(adminService.searchSurveys(query), "surveys", page, size)
                .doOnSuccess(response -> logger.info("Survey search completed successfully"))
                .doOnError(e -> errorLogger.error("Error during survey search: {}", e.getMessage(), e));
    }

    @GetMapping("/search/questions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search questions",
        description = "Searches for the query string in question content with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchQuestions(
            @Parameter(description = "Search query string", required = true)
            @RequestParam String query,
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Searching questions with query: '{}', page: {}, size: {}", query, page, size);
        return ResponseUtils.wrapFluxResponsePaginated(adminService.searchQuestions(query), "questions", page, size)
                .doOnSuccess(response -> logger.info("Question search completed successfully"))
                .doOnError(e -> errorLogger.error("Error during question search: {}", e.getMessage(), e));
    }
    
    @GetMapping("/search/questions/survey")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search questions by survey ID",
        description = "Retrieves all questions belonging to a specific survey with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid survey ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchQuestionsBySurveyId(
            @Parameter(description = "Survey ID", required = true, example = "1")
            @RequestParam Integer surveyId,
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Searching questions for survey ID: {}, page: {}, size: {}", surveyId, page, size);
        return ResponseUtils.wrapFluxResponsePaginated(
            adminService.searchQuestionsBySurveyId(surveyId), 
            "questions for survey " + surveyId,
            page,
            size
        )
        .doOnSuccess(response -> logger.info("Question search by survey ID completed successfully"))
        .doOnError(e -> errorLogger.error("Error searching questions for survey ID {}: {}", surveyId, e.getMessage(), e));
    }
    
    @GetMapping("/search/questions/type")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search questions by type",
        description = "Retrieves all questions of a specific question type with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid question type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchQuestionsByType(
            @Parameter(description = "Question type", required = true, example = "MULTIPLE")
            @RequestParam String type,
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Searching questions of type: {} with pagination - page: {}, size: {}", type, page, size);
        return ResponseUtils.wrapFluxResponsePaginated(
            adminService.searchQuestionsByType(type), 
            "questions of type " + type,
            page,
            size
        )
        .doOnSuccess(response -> logger.info("Question search by type completed successfully"))
        .doOnError(e -> errorLogger.error("Error searching questions of type {}: {}", type, e.getMessage(), e));
    }

    @GetMapping("/search/choices")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search choices",
        description = "Searches for the query string in choice text with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchChoices(
            @Parameter(description = "Search query string", required = true)
            @RequestParam String query,
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Searching choices with query: '{}', page: {}, size: {}", query, page, size);
        return ResponseUtils.wrapFluxResponsePaginated(adminService.searchChoices(query), "choices", page, size)
                .doOnSuccess(response -> logger.info("Choice search completed successfully"))
                .doOnError(e -> errorLogger.error("Error during choice search: {}", e.getMessage(), e));
    }
    
    @GetMapping("/search/choices/question")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search choices by question ID",
        description = "Retrieves all choices belonging to a specific question with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid question ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchChoicesByQuestionId(
            @Parameter(description = "Question ID", required = true, example = "1")
            @RequestParam Integer questionId,
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Searching choices for question ID: {}, page: {}, size: {}", questionId, page, size);
        return ResponseUtils.wrapFluxResponsePaginated(
            adminService.searchChoicesByQuestionId(questionId), 
            "choices for question " + questionId,
            page,
            size
        )
        .doOnSuccess(response -> logger.info("Choice search by question ID completed successfully"))
        .doOnError(e -> errorLogger.error("Error searching choices for question ID {}: {}", questionId, e.getMessage(), e));
    }

    @GetMapping("/search/answers/question")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search answers by question ID",
        description = "Retrieves all answers for a specific question with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid question ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchAnswersByQuestionId(
            @Parameter(description = "Question ID", required = true, example = "1")
            @RequestParam Integer questionId,
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Searching answers for question ID: {}, page: {}, size: {}", questionId, page, size);
        return ResponseUtils.wrapFluxResponsePaginated(
            adminService.searchAnswersByQuestionId(questionId), 
            "answers for question " + questionId,
            page,
            size
        )
        .doOnSuccess(response -> logger.info("Answer search by question ID completed successfully"))
        .doOnError(e -> errorLogger.error("Error searching answers for question ID {}: {}", questionId, e.getMessage(), e));
    }
    
    @GetMapping("/search/answers/user")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search answers by user ID",
        description = "Retrieves all answers submitted by a specific user with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchAnswersByUserId(
            @Parameter(description = "User ID", required = true, example = "1")
            @RequestParam Integer userId,
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Searching answers for user ID: {}, page: {}, size: {}", userId, page, size);
        return ResponseUtils.wrapFluxResponsePaginated(
            adminService.searchAnswersByUserId(userId), 
            "answers by user " + userId,
            page,
            size
        )
        .doOnSuccess(response -> logger.info("Answer search by user ID completed successfully"))
        .doOnError(e -> errorLogger.error("Error searching answers for user ID {}: {}", userId, e.getMessage(), e));
    }

    @GetMapping("/search/answers/public")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search public answers",
        description = "Retrieves all answers marked as public with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> searchPublicAnswers(
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Searching for public answers with pagination - page: {}, size: {}", page, size);
        return ResponseUtils.wrapFluxResponsePaginated(
            adminService.searchPublicAnswers(), 
            "public answers",
            page,
            size
        )
        .doOnSuccess(response -> logger.info("Public answer search completed successfully"))
        .doOnError(e -> errorLogger.error("Error searching for public answers: {}", e.getMessage(), e));
    }
    
    @GetMapping("/search/answers/question-user")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search answers by question ID and user ID",
        description = "Retrieves all answers for a specific question submitted by a specific user with optional pagination"
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
            @RequestParam Integer userId,
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Searching answers for question ID: {} and user ID: {}, page: {}, size: {}", questionId, userId, page, size);
        return ResponseUtils.wrapFluxResponsePaginated(
            adminService.searchAnswersByQuestionIdAndUserId(questionId, userId), 
            "answers for question " + questionId + " by user " + userId,
            page,
            size
        )
        .doOnSuccess(response -> logger.info("Answer search by question ID and user ID completed successfully"))
        .doOnError(e -> errorLogger.error("Error searching answers for question ID {} and user ID {}: {}", 
                          questionId, userId, e.getMessage(), e));
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
        logger.info("Starting Elasticsearch synchronization");
        
        if (elasticsearchSyncService == null) {
            logger.warn("Elasticsearch synchronization requested but Elasticsearch is disabled");
            return Mono.just(ResponseEntity.ok(Map.of(
                "status", "skipped",
                "message", "Elasticsearch is disabled"
            )));
        }
        
        return elasticsearchSyncService.syncAllData()
                .thenReturn(ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Elasticsearch synchronization completed successfully"
                )))
                .doOnSuccess(response -> logger.info("Elasticsearch synchronization completed successfully"))
                .doOnError(e -> errorLogger.error("Error during Elasticsearch synchronization: {}", e.getMessage(), e));
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
        logger.info("Retrieving system statistics");
        return adminService.getStatistics()
                .map(stats -> {
                    logger.debug("Statistics retrieved: {}", stats);
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "data", stats
                    );
                    return ResponseEntity.ok((Object) response);
                })
                .doOnSuccess(response -> logger.info("System statistics retrieved successfully"))
                .doOnError(e -> errorLogger.error("Error retrieving system statistics: {}", e.getMessage(), e));
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
        logger.info("Retrieving question type statistics");
        return adminService.getQuestionTypeStatistics()
                .map(stats -> {
                    logger.debug("Question type statistics retrieved: {}", stats);
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "data", stats
                    );
                    return ResponseEntity.ok((Object) response);
                })
                .doOnSuccess(response -> logger.info("Question type statistics retrieved successfully"))
                .doOnError(e -> errorLogger.error("Error retrieving question type statistics: {}", e.getMessage(), e));
    }
    
    @GetMapping("/statistics/user-participation")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get user participation statistics",
        description = "Retrieves statistics about user participation and answer counts with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public Mono<ResponseEntity<Object>> getUserParticipationStatistics(
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Retrieving user participation statistics with pagination - page: {}, size: {}", page, size);
        return ResponseUtils.wrapFluxResponsePaginated(
                adminService.getUserParticipationStatistics(),
                "user participation statistics",
                page,
                size
            )
            .doOnSuccess(response -> logger.info("User participation statistics retrieved successfully"))
            .doOnError(e -> errorLogger.error("Error retrieving user participation statistics: {}", e.getMessage(), e));
    }
} 