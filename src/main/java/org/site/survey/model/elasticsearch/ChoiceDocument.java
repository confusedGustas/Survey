package org.site.survey.model.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "choices")
public class ChoiceDocument {
    @Id
    private Integer id;
    
    @Field(type = FieldType.Integer, name = "question_id")
    private Integer questionId;
    
    @Field(type = FieldType.Text, name = "choice_text")
    private String choiceText;
} 