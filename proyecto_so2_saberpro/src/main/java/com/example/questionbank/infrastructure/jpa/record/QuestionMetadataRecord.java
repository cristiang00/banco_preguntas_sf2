package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.CognitiveLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity record for QuestionMetadata persistence.
 *
 * <p>Represents the target of the @OneToOne relationship with QuestionRecord.</p>
 */
@Entity
@Table(name = "question_metadata")
public class QuestionMetadataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_review_date")
    private LocalDateTime lastReviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "cognitive_level", length = 50)
    private CognitiveLevel cognitiveLevel;

    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes;

    @Column(name = "editorial_notes", length = 2000)
    private String editorialNotes;

    @OneToOne(mappedBy = "metadata", fetch = FetchType.LAZY)
    private QuestionRecord question;

    public QuestionMetadataRecord() {
    }

    public QuestionMetadataRecord(Long id, LocalDateTime lastReviewDate, CognitiveLevel cognitiveLevel,
                                  Integer estimatedTimeMinutes, String editorialNotes) {
        this.id = id;
        this.lastReviewDate = lastReviewDate;
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

    public QuestionRecord getQuestion() {
        return question;
    }

    public void setQuestion(QuestionRecord question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionMetadataRecord that = (QuestionMetadataRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
