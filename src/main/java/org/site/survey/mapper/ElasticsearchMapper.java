package org.site.survey.mapper;

import org.site.survey.model.Answer;
import org.site.survey.model.Choice;
import org.site.survey.model.Question;
import org.site.survey.model.Survey;
import org.site.survey.model.elasticsearch.AnswerDocument;
import org.site.survey.model.elasticsearch.ChoiceDocument;
import org.site.survey.model.elasticsearch.QuestionDocument;
import org.site.survey.model.elasticsearch.SurveyDocument;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchMapper {

    public SurveyDocument mapToSurveyDocument(Survey survey, int questionCount) {
        SurveyDocument doc = new SurveyDocument();
        doc.setId(survey.getId());
        doc.setTitle(survey.getTitle());
        doc.setDescription(survey.getDescription());
        doc.setQuestionSize(questionCount);
        doc.setCreatedBy(survey.getCreatedBy());
        doc.setCreatedAt(survey.getCreatedAt());
        return doc;
    }
    
    public QuestionDocument mapToQuestionDocument(Question question) {
        QuestionDocument doc = new QuestionDocument();
        doc.setId(question.getId());
        doc.setSurveyId(question.getSurveyId());
        doc.setContent(question.getContent());
        doc.setQuestionType(question.getQuestionType());
        doc.setCreatedAt(question.getCreatedAt());
        return doc;
    }
    
    public ChoiceDocument mapToChoiceDocument(Choice choice) {
        ChoiceDocument doc = new ChoiceDocument();
        doc.setId(choice.getId());
        doc.setQuestionId(choice.getQuestionId());
        doc.setChoiceText(choice.getChoiceText());
        return doc;
    }
    
    public AnswerDocument mapToAnswerDocument(Answer answer) {
        AnswerDocument doc = new AnswerDocument();
        doc.setId(answer.getId());
        doc.setQuestionId(answer.getQuestionId());
        doc.setUserId(answer.getUserId());
        doc.setChoiceId(answer.getChoiceId());
        doc.setIsPublic(answer.getIsPublic());
        doc.setCreatedAt(answer.getCreatedAt());
        return doc;
    }
}