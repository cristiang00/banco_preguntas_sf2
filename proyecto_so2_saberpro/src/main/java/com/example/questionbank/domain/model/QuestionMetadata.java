package com.example.questionbank.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing complementary metadata for a question.
 *
 * <p>Enforces a 1-to-1 relationship with Question.
 * Includes cognitive level, estimated response time in minutes,
 * last review timestamp, and editorial notes.</p>
 */
public class QuestionMetadata {

    private Long id;
    private LocalDateTime lastReviewDate;
    private CognitiveLevel cognitiveLevel;
    private Integer estimatedTimeMinutes;
    private String editorialNotes;

    public QuestionMetadata() {
    }

    public QuestionMetadata(Long id, LocalDateTime lastReviewDate, CognitiveLevel cognitiveLevel,
                            Integer estimatedTimeMinutes, String editorialNotes) {
        this.id = id;
        this.lastReviewDate = lastReviewDate;
        this.cognitiveLevel = cognitiveLevel;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.editorialNotes = editorialNotes;
    }

    public QuestionMetadata(CognitiveLevel cognitiveLevel, Integer estimatedTimeMinutes, String editorialNotes) {
        this(null, LocalDateTime.now(), cognitiveLevel, estimatedTimeMinutes, editorialNotes);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionMetadata that = (QuestionMetadata) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "QuestionMetadata{" +
                "id=" + id +
                ", cognitiveLevel=" + cognitiveLevel +
                ", estimatedTimeMinutes=" + estimatedTimeMinutes +
                '}';
    }
}
