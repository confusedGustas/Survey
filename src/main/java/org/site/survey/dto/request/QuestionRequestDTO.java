package org.site.survey.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.site.survey.type.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDTO {
    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content must be less than 2000 characters")
    private String content;
    
    @NotNull(message = "Question type is required")
    private QuestionType questionType;
    
    private List<String> choices;
    
    @JsonCreator
    public static QuestionRequestDTO create(
            @JsonProperty("content") String content,
            @JsonProperty("questionType") String questionType,
            @JsonProperty("choices") List<String> choices) {
        
        QuestionType type = null;
        try {
            if (questionType != null) {
                type = QuestionType.valueOf(questionType.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            type = QuestionType.SINGLE;
        }
        
        return QuestionRequestDTO.builder()
                .content(content)
                .questionType(type)
                .choices(choices)
                .build();
    }
} 