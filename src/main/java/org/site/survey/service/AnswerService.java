package org.site.survey.service;

import lombok.RequiredArgsConstructor;
import org.site.survey.dto.request.QuestionAnswerDTO;
import org.site.survey.dto.request.SurveyAnswerRequestDTO;
import org.site.survey.dto.response.AnswerResponseDTO;
import org.site.survey.dto.response.GroupedSurveyAnswerResponseDTO;
import org.site.survey.dto.response.QuestionGroupedAnswerDTO;
import org.site.survey.dto.response.SurveyAnswerResponseDTO;
import org.site.survey.exception.ChoiceNotFoundException;
import org.site.survey.exception.InvalidAnswerFormatException;
import org.site.survey.exception.QuestionNotFoundException;
import org.site.survey.exception.SurveyNotFoundException;
import org.site.survey.model.Answer;
import org.site.survey.model.Question;
import org.site.survey.repository.AnswerRepository;
import org.site.survey.repository.ChoiceRepository;
import org.site.survey.repository.QuestionRepository;
import org.site.survey.repository.SurveyRepository;
import org.site.survey.type.QuestionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final SurveyRepository surveyRepository;

    @Transactional
    public Mono<SurveyAnswerResponseDTO> submitSurveyAnswers(SurveyAnswerRequestDTO request, Integer userId) {
        return surveyRepository.findById(request.getSurveyId())
                .switchIfEmpty(Mono.error(new SurveyNotFoundException()))
                .flatMap(survey -> questionRepository.findBySurveyId(survey.getId())
                        .collectList()
                        .flatMap(surveyQuestions -> {
                            Map<Integer, Question> questionMap = surveyQuestions.stream()
                                    .collect(Collectors.toMap(Question::getId, q -> q));

                            List<Integer> surveyQuestionIds = surveyQuestions.stream()
                                    .map(Question::getId)
                                    .toList();

                            List<Integer> answeredQuestionIds = request.getAnswers().stream()
                                    .map(QuestionAnswerDTO::getQuestionId)
                                    .toList();

                            if (!new HashSet<>(surveyQuestionIds).containsAll(answeredQuestionIds) ||
                                !new HashSet<>(answeredQuestionIds).containsAll(surveyQuestionIds)) {
                                return Mono.error(new InvalidAnswerFormatException("All questions in the survey must be answered"));
                            }

                            for (QuestionAnswerDTO answer : request.getAnswers()) {
                                Question question = questionMap.get(answer.getQuestionId());
                                if (question == null) {
                                    return Mono.error(new QuestionNotFoundException());
                                }

                                QuestionType type = question.getQuestionTypeEnum();

                                if (type == QuestionType.SINGLE) {
                                    if (answer.getChoiceId() == null) {
                                        return Mono.error(new InvalidAnswerFormatException(
                                                "SINGLE type question requires exactly one choice"));
                                    }
                                    if (answer.getChoiceIds() != null || answer.getTextResponse() != null) {
                                        return Mono.error(new InvalidAnswerFormatException(
                                                "SINGLE type question should only have choiceId field"));
                                    }
                                } else if (type == QuestionType.MULTIPLE) {
                                    if (answer.getChoiceIds() == null || answer.getChoiceIds().isEmpty()) {
                                        return Mono.error(new InvalidAnswerFormatException(
                                                "MULTIPLE type question requires at least one choice"));
                                    }
                                    if (answer.getChoiceId() != null || answer.getTextResponse() != null) {
                                        return Mono.error(new InvalidAnswerFormatException(
                                                "MULTIPLE type question should only have choiceIds field"));
                                    }
                                } else if (type == QuestionType.TEXT) {
                                    if (answer.getTextResponse() == null || answer.getTextResponse().trim().isEmpty()) {
                                        return Mono.error(new InvalidAnswerFormatException(
                                                "TEXT type question requires a text response"));
                                    }
                                    if (answer.getChoiceId() != null || answer.getChoiceIds() != null) {
                                        return Mono.error(new InvalidAnswerFormatException(
                                                "TEXT type question should only have textResponse field"));
                                    }
                                }
                            }

                            List<Mono<AnswerResponseDTO>> answerMonos = new ArrayList<>();

                            for (QuestionAnswerDTO answerDTO : request.getAnswers()) {
                                Question question = questionMap.get(answerDTO.getQuestionId());
                                QuestionType type = question.getQuestionTypeEnum();

                                if (type == QuestionType.SINGLE) {
                                    Answer newAnswer = Answer.builder()
                                            .questionId(answerDTO.getQuestionId())
                                            .userId(userId)
                                            .choiceId(answerDTO.getChoiceId())
                                            .isPublic(true)
                                            .createdAt(LocalDateTime.now())
                                            .build();

                                    Mono<AnswerResponseDTO> answerMono = choiceRepository.findById(answerDTO.getChoiceId())
                                            .switchIfEmpty(Mono.error(new ChoiceNotFoundException()))
                                            .flatMap(choice -> answerRepository.save(newAnswer)
                                                    .map(savedAnswer -> buildAnswerResponse(savedAnswer, choice.getChoiceText())));

                                    answerMonos.add(answerMono);
                                } else if (type == QuestionType.TEXT) {
                                    Answer newAnswer = Answer.builder()
                                            .questionId(answerDTO.getQuestionId())
                                            .userId(userId)
                                            .choiceId(null)
                                            .isPublic(true)
                                            .createdAt(LocalDateTime.now())
                                            .build();

                                    Mono<AnswerResponseDTO> answerMono = answerRepository.save(newAnswer)
                                            .map(savedAnswer -> buildAnswerResponse(savedAnswer, answerDTO.getTextResponse()));

                                    answerMonos.add(answerMono);
                                } else if (type == QuestionType.MULTIPLE) {
                                    for (Integer choiceId : answerDTO.getChoiceIds()) {
                                        Answer newAnswer = Answer.builder()
                                                .questionId(answerDTO.getQuestionId())
                                                .userId(userId)
                                                .choiceId(choiceId)
                                                .isPublic(true)
                                                .createdAt(LocalDateTime.now())
                                                .build();

                                        Mono<AnswerResponseDTO> answerMono = choiceRepository.findById(choiceId)
                                                .switchIfEmpty(Mono.error(new ChoiceNotFoundException()))
                                                .flatMap(choice -> answerRepository.save(newAnswer)
                                                        .map(savedAnswer -> buildAnswerResponse(savedAnswer, choice.getChoiceText())));

                                        answerMonos.add(answerMono);
                                    }
                                }
                            }

                            return Flux.concat(answerMonos)
                                    .collectList()
                                    .map(answers -> SurveyAnswerResponseDTO.builder()
                                            .surveyId(request.getSurveyId())
                                            .userId(userId)
                                            .submittedAt(LocalDateTime.now())
                                            .answers(answers)
                                            .build());
                        }));
    }

    @Transactional
    public Mono<GroupedSurveyAnswerResponseDTO> submitSurveyAnswersGrouped(SurveyAnswerRequestDTO request, Integer userId) {
        return submitSurveyAnswers(request, userId)
                .map(this::transformToGroupedResponse);
    }

    private GroupedSurveyAnswerResponseDTO transformToGroupedResponse(SurveyAnswerResponseDTO response) {
        Map<Integer, List<AnswerResponseDTO>> answersByQuestion = response.getAnswers().stream()
                .collect(Collectors.groupingBy(AnswerResponseDTO::getQuestionId));

        List<QuestionGroupedAnswerDTO> groupedAnswers = answersByQuestion.entrySet().stream()
                .map(entry -> {
                    Integer questionId = entry.getKey();
                    List<AnswerResponseDTO> answers = entry.getValue();

                    QuestionType questionType;
                    if (answers.size() > 1) {
                        questionType = QuestionType.MULTIPLE;
                    } else if (answers.get(0).getChoiceId() == null) {
                        questionType = QuestionType.TEXT;
                        answers.forEach(answer -> answer.setChoiceId(null));
                    } else {
                        questionType = QuestionType.SINGLE;
                    }
                    
                    return QuestionGroupedAnswerDTO.builder()
                            .questionId(questionId)
                            .questionType(questionType)
                            .answers(answers)
                            .build();
                })
                .collect(Collectors.toList());
        
        return GroupedSurveyAnswerResponseDTO.builder()
                .surveyId(response.getSurveyId())
                .userId(response.getUserId())
                .submittedAt(response.getSubmittedAt())
                .answers(groupedAnswers)
                .build();
    }

    private AnswerResponseDTO buildAnswerResponse(Answer answer, String choiceText) {
        return AnswerResponseDTO.builder()
                .id(answer.getId())
                .questionId(answer.getQuestionId())
                .userId(answer.getUserId())
                .choiceId(answer.getChoiceId())
                .isPublic(answer.getIsPublic())
                .createdAt(answer.getCreatedAt())
                .choiceText(choiceText)
                .build();
    }
} 