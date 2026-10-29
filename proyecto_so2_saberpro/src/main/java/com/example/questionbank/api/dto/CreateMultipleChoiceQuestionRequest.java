package com.example.questionbank.api.dto;

import com.example.questionbank.domain.model.Difficulty;

import java.util.List;

public class CreateMultipleChoiceQuestionRequest {
    private String statement;
    private Difficulty difficulty;
    private Long authorId;
    private List<AnswerOptionDto> options;
    private QuestionMetadataDto metadata;

    public CreateMultipleChoiceQuestionRequest() {
    }

    public CreateMultipleChoiceQuestionRequest(String statement, Difficulty difficulty, Long authorId,
                                              List<AnswerOptionDto> options, QuestionMetadataDto metadata) {
        this.statement = statement;
        this.difficulty = difficulty;
        this.authorId = authorId;
        this.options = options;
        this.metadata = metadata;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public List<AnswerOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<AnswerOptionDto> options) {
        this.options = options;
    }

    public QuestionMetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(QuestionMetadataDto metadata) {
        this.metadata = metadata;
    }
}
