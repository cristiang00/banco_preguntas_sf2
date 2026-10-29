package com.example.questionbank.api.dto;

import java.util.List;

public class CreateQuizRequest {
    private String title;
    private String description;
    private Long createdById;
    private List<Long> questionIds;
    private Integer totalScore;

    public CreateQuizRequest() {
    }

    public CreateQuizRequest(String title, String description, Long createdById, List<Long> questionIds, Integer totalScore) {
        this.title = title;
        this.description = description;
        this.createdById = createdById;
        this.questionIds = questionIds;
        this.totalScore = totalScore;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public List<Long> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<Long> questionIds) {
        this.questionIds = questionIds;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
}
