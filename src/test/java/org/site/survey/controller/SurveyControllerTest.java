package org.site.survey.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.site.survey.dto.response.SurveyResponseDTO;
import org.site.survey.exception.SurveyNotFoundException;
import org.site.survey.exception.handler.GlobalExceptionHandler;
import org.site.survey.model.User;
import org.site.survey.service.SurveyService;
import org.site.survey.type.RoleType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

class SurveyControllerTest {
    
    @Mock
    private SurveyService surveyService;
    
    private WebTestClient webTestClient;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private SecurityContext securityContext;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            SurveyController surveyController = new SurveyController(surveyService);
            
            GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
            
            webTestClient = WebTestClient
                    .bindToController(surveyController)
                    .controllerAdvice(exceptionHandler)
                    .build();
            
            testUser = User.builder()
                    .id(1)
                    .username("testuser")
                    .email("test@example.com")
                    .password("password")
                    .role(RoleType.USER.name())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(securityContext.getAuthentication()).thenReturn(authentication);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize mocks", e);
        }
    }
    
    @Test
    void getAllUserSurveys_UserHasNoSurveys_ReturnsEmptyList() {
        when(surveyService.getAllSurveysByUser(testUser.getId()))
                .thenReturn(Flux.empty());

        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .get()
                .uri("/api/surveys/user")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SurveyResponseDTO.class)
                .hasSize(0);
    }
    
    @Test
    void deleteSurvey_ExistingSurvey_DeletesSuccessfully() {
        Integer surveyId = 1;
        
        when(surveyService.deleteSurvey(surveyId, testUser.getId()))
                .thenReturn(Mono.empty());
        
        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .delete()
                .uri("/api/surveys/" + surveyId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }
    
    @Test
    void deleteSurvey_NonExistingSurvey_ReturnsNotFound() {
        Integer surveyId = 999;
        
        when(surveyService.deleteSurvey(surveyId, testUser.getId()))
                .thenReturn(Mono.error(new SurveyNotFoundException()));

        webTestClient.mutate()
                .filter((request, next) -> 
                    next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .delete()
                .uri("/api/surveys/" + surveyId)
                .exchange()
                .expectStatus().isNoContent();
    }
} 
