package com.example.questionbank.api.dto;

import com.example.questionbank.domain.model.CognitiveLevel;

import java.time.LocalDateTime;

public class QuestionMetadataDto {
    private Long id;
    private LocalDateTime lastReviewDate;
    private CognitiveLevel cognitiveLevel;
    private Integer estimatedTimeMinutes;
    private String editorialNotes;

    public QuestionMetadataDto() {
    }

    public QuestionMetadataDto(CognitiveLevel cognitiveLevel, Integer estimatedTimeMinutes, String editorialNotes) {
        this.cognitiveLevel = cognitiveLevel;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.editorialNotes = editorialNotes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getLastReviewDate() {
        return lastReviewDate;
    }

    public void setLastReviewDate(LocalDateTime lastReviewDate) {
        this.lastReviewDate = lastReviewDate;
    }

    public CognitiveLevel getCognitiveLevel() {
        return cognitiveLevel;
    }

    public void setCognitiveLevel(CognitiveLevel cognitiveLevel) {
        this.cognitiveLevel = cognitiveLevel;
    }

    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public String getEditorialNotes() {
        return editorialNotes;
    }

    public void setEditorialNotes(String editorialNotes) {
        this.editorialNotes = editorialNotes;
    }
}
