package org.site.survey.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.site.survey.dto.request.SurveyRequestDTO;
import org.site.survey.dto.response.SurveyResponseDTO;
import org.site.survey.model.User;
import org.site.survey.service.SurveyService;
import org.site.survey.util.LoggerUtil;
import org.site.survey.util.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Tag(name = "Survey Management", description = "APIs for managing surveys")
public class SurveyController {
    
    private static final Logger logger = LoggerUtil.getLogger(SurveyController.class);
    private static final Logger errorLogger = LoggerUtil.getErrorLogger(SurveyController.class);
    
    private final SurveyService surveyService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create new survey",
        description = "Creates a new survey with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Survey created successfully",
            content = @Content(schema = @Schema(implementation = SurveyResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<Object>> createSurvey(
            @Parameter(description = "Survey details", required = true)
            @Valid @RequestBody SurveyRequestDTO request) {
        logger.info("Creating new survey with title: {}", request.getTitle());
        logger.debug("Survey creation request details: {}", request);
        
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(User.class)
                .flatMap(user -> {
                    logger.info("Creating survey for user ID: {}", user.getId());
                    return surveyService.createSurvey(request, user.getId());
                })
                .map(survey -> {
                    logger.info("Survey created successfully with ID: {}", survey.getId());
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "data", survey
                    );
                    return ResponseEntity.status(HttpStatus.CREATED).body((Object) response);
                })
                .doOnError(error -> errorLogger.error("Failed to create survey: {}", error.getMessage(), error));
    }

    @GetMapping("/user")
    @Operation(
        summary = "Get all user surveys",
        description = "Retrieves all surveys created by the current authenticated user with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Surveys retrieved successfully",
            content = @Content(schema = @Schema(implementation = SurveyResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<Object>> getAllUserSurveys(
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Retrieving paginated surveys for current user - page: {}, size: {}", page, size);
        
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(User.class)
                .flatMap(user -> {
                    logger.info("Fetching surveys for user ID: {}", user.getId());
                    Flux<SurveyResponseDTO> surveys = surveyService.getAllSurveysByUser(user.getId());
                    return ResponseUtils.wrapFluxResponsePaginated(surveys, "surveys for user " + user.getId(), page, size);
                })
                .doOnError(error -> errorLogger.error("Failed to retrieve user surveys: {}", error.getMessage(), error));
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete survey",
        description = "Deletes a survey by its ID if the current user is the creator"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Survey deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid survey ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user is not the survey creator"),
        @ApiResponse(responseCode = "404", description = "Survey not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<Void> deleteSurvey(
            @Parameter(description = "ID of the survey to delete", required = true)
            @PathVariable Integer id) {
        logger.info("Deleting survey with ID: {}", id);
        
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(User.class)
                .flatMap(user -> {
                    logger.info("User {} attempting to delete survey {}", user.getId(), id);
                    return surveyService.deleteSurvey(id, user.getId());
                })
                .doOnSuccess(result -> logger.info("Successfully deleted survey with ID: {}", id))
                .doOnError(error -> errorLogger.error("Failed to delete survey with ID {}: {}", id, error.getMessage(), error));
    }

    @GetMapping("/all")
    @Operation(
        summary = "Get all surveys",
        description = "Retrieves all surveys with optional pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Surveys retrieved successfully",
            content = @Content(schema = @Schema(implementation = SurveyResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<Object>> getAllSurveys(
            @Parameter(description = "Page number (0-based)", schema = @Schema(defaultValue = "0"))
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size", schema = @Schema(defaultValue = "10"))
            @RequestParam(required = false, defaultValue = "10") int size) {
        logger.info("Retrieving paginated all surveys - page: {}, size: {}", page, size);
        Flux<SurveyResponseDTO> surveys = surveyService.getAllSurveys();
        return ResponseUtils.wrapFluxResponsePaginated(surveys, "all surveys", page, size)
                .doOnError(error -> errorLogger.error("Failed to retrieve all surveys: {}", error.getMessage(), error));
    }
} 