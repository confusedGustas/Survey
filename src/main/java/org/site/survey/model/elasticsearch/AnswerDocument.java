package org.site.survey.model.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "answers")
public class AnswerDocument {
    @Id
    private Integer id;
    
    @Field(type = FieldType.Integer, name = "question_id")
    private Integer questionId;
    
    @Field(type = FieldType.Integer, name = "user_id")
    private Integer userId;
    
    @Field(type = FieldType.Integer, name = "choice_id")
    private Integer choiceId;
    
    @Field(type = FieldType.Boolean, name = "is_public")
    private Boolean isPublic;
    
    @Field(type = FieldType.Date, name = "created_at", format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;
} 