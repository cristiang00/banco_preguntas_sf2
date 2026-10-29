package com.example.questionbank.domain.model;

import java.util.Set;

/**
 * Domain entity representing a True/False Question.
 *
 * <p>Evaluates a statement against an expected Boolean truth value.</p>
 */
public class TrueFalseQuestion extends Question {

    private boolean expectedTruthValue;

    public TrueFalseQuestion() {
        super();
    }

    public TrueFalseQuestion(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                             User author, Topic topic, Set<Competency> competencies, QuestionMetadata metadata,
                             boolean expectedTruthValue) {
        super(id, statement, difficulty, status, author, topic, competencies, metadata);
        this.expectedTruthValue = expectedTruthValue;
    }

    public TrueFalseQuestion(String statement, Difficulty difficulty, User author, boolean expectedTruthValue) {
        super(null, statement, difficulty, QuestionStatus.DRAFT, author, null, null, null);
        this.expectedTruthValue = expectedTruthValue;
    }

    @Override
    public void validateForReview() {
        // True/false questions only require a valid statement and boolean value (already present).
        if (getStatement() == null || getStatement().trim().isEmpty()) {
            throw new IllegalStateException("Statement cannot be empty for True/False question.");
        }
    }

    public boolean isExpectedTruthValue() {
        return expectedTruthValue;
    }

    public void setExpectedTruthValue(boolean expectedTruthValue) {
        this.expectedTruthValue = expectedTruthValue;
    }
}
