package com.example.questionbank.domain.model;

import java.util.Objects;

/**
 * Domain entity representing an answer option (distractor or correct choice) for a Question.
 *
 * <p>Enforces that an option belongs to a single question (1-to-many relationship).</p>
 */
public class AnswerOption {

    private Long id;
    private String text;
    private boolean correct;
    private String explanation;

    public AnswerOption() {
    }

    public AnswerOption(Long id, String text, boolean correct, String explanation) {
        this.id = id;
        this.text = text;
        this.correct = correct;
        this.explanation = explanation;
    }

    public AnswerOption(String text, boolean correct, String explanation) {
        this(null, text, correct, explanation);
    }

    public AnswerOption(String text, boolean correct) {
        this(null, text, correct, null);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerOption that = (AnswerOption) o;
        return Objects.equals(id, that.id) || (Objects.equals(text, that.text) && correct == that.correct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, correct);
    }

    @Override
    public String toString() {
        return "AnswerOption{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", correct=" + correct +
                '}';
    }
}
