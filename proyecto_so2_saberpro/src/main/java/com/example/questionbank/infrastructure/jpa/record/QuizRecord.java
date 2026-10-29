package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA entity record for Quiz/Mock Exam persistence.
 *
 * <p>Contains a @ManyToMany relationship to QuestionRecord via join table 'quiz_questions'.</p>
 */
@Entity
@Table(name = "quizzes")
public class QuizRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private UserRecord createdBy;

    @Column(name = "total_score")
    private Integer totalScore;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "quiz_questions",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<QuestionRecord> questions = new ArrayList<>();

    public QuizRecord() {
    }

    public QuizRecord(Long id, String title, String description, UserRecord createdBy, Integer totalScore) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.totalScore = totalScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public UserRecord getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserRecord createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public List<QuestionRecord> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionRecord> questions) {
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizRecord that = (QuizRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
