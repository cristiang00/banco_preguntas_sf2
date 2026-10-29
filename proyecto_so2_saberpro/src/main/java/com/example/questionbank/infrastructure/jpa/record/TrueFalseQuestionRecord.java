package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * JPA entity record specializing QuestionRecord for True/False Questions.
 *
 * <p>Stored in the child table 'tf_questions' linked by primary key to 'questions'.</p>
 */
@Entity
@Table(name = "tf_questions")
@DiscriminatorValue("TRUE_FALSE")
@PrimaryKeyJoinColumn(name = "id")
public class TrueFalseQuestionRecord extends QuestionRecord {

    @Column(name = "expected_truth_value", nullable = false)
    private boolean expectedTruthValue;

    public TrueFalseQuestionRecord() {
        super();
    }

    public TrueFalseQuestionRecord(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                                  UserRecord author, TopicRecord topic, QuestionMetadataRecord metadata,
                                  boolean expectedTruthValue) {
        super(id, statement, difficulty, status, author, topic, metadata);
        this.expectedTruthValue = expectedTruthValue;
    }

    public boolean isExpectedTruthValue() {
        return expectedTruthValue;
    }

    public void setExpectedTruthValue(boolean expectedTruthValue) {
        this.expectedTruthValue = expectedTruthValue;
    }
}
