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
@Document(indexName = "surveys")
public class SurveyDocument {
    @Id
    private Integer id;
    
    @Field(type = FieldType.Text, name = "title")
    private String title;
    
    @Field(type = FieldType.Text, name = "description")
    private String description;
    
    @Field(type = FieldType.Integer, name = "question_size")
    private Integer questionSize;
    
    @Field(type = FieldType.Integer, name = "created_by")
    private Integer createdBy;
    
    @Field(type = FieldType.Date, name = "created_at", format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;
} 