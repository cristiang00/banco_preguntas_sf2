package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * JPA entity record for AnswerOption persistence.
 *
 * <p>Represents the owning side of the @OneToMany relationship from MultipleChoiceQuestionRecord.
 * The foreign key 'question_id' is non-nullable to prevent orphan options.</p>
 */
@Entity
@Table(name = "answer_options")
public class AnswerOptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(length = 1000)
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private MultipleChoiceQuestionRecord question;

    public AnswerOptionRecord() {
    }

    public AnswerOptionRecord(Long id, String text, boolean correct, String explanation) {
        this.id = id;
        this.text = text;
        this.correct = correct;
        this.explanation = explanation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public MultipleChoiceQuestionRecord getQuestion() {
        return question;
    }

    public void setQuestion(MultipleChoiceQuestionRecord question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerOptionRecord that = (AnswerOptionRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
