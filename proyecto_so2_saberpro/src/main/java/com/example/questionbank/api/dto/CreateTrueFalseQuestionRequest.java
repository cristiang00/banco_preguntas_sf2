package com.example.questionbank.api.dto;

import com.example.questionbank.domain.model.Difficulty;

public class CreateTrueFalseQuestionRequest {
    private String statement;
    private Difficulty difficulty;
    private Long authorId;
    private boolean expectedTruthValue;
    private QuestionMetadataDto metadata;

    public CreateTrueFalseQuestionRequest() {
    }

    public CreateTrueFalseQuestionRequest(String statement, Difficulty difficulty, Long authorId,
                                         boolean expectedTruthValue, QuestionMetadataDto metadata) {
        this.statement = statement;
        this.difficulty = difficulty;
        this.authorId = authorId;
        this.expectedTruthValue = expectedTruthValue;
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

    public boolean isExpectedTruthValue() {
        return expectedTruthValue;
    }

    public void setExpectedTruthValue(boolean expectedTruthValue) {
        this.expectedTruthValue = expectedTruthValue;
    }

    public QuestionMetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(QuestionMetadataDto metadata) {
        this.metadata = metadata;
    }
}
