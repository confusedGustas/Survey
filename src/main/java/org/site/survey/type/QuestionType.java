package org.site.survey.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum QuestionType {
    SINGLE,
    MULTIPLE,
    TEXT;
    
    @JsonValue
    public String getValue() {
        return this.name();
    }
    
    @JsonCreator
    public static QuestionType fromValue(@JsonProperty("questionType") String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return QuestionType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SINGLE;
        }
    }
} 