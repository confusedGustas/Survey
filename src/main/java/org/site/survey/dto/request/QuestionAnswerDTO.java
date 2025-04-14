package org.site.survey.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswerDTO {
    @NotNull(message = "Question ID is required")
    private Integer questionId;

    private Integer choiceId;

    private List<Integer> choiceIds;

    @Size(max = 2000, message = "Response must be less than 2000 characters")
    private String textResponse;
} 