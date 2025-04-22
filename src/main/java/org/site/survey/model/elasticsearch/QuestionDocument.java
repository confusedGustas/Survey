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
@Document(indexName = "questions")
public class QuestionDocument {
    @Id
    private Integer id;
    
    @Field(type = FieldType.Integer, name = "survey_id")
    private Integer surveyId;
    
    @Field(type = FieldType.Text, name = "content")
    private String content;
    
    @Field(type = FieldType.Keyword, name = "question_type")
    private String questionType;
    
    @Field(type = FieldType.Date, name = "created_at", format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;
} 