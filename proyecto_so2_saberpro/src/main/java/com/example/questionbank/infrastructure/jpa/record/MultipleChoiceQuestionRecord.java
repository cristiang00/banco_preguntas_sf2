package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity record specializing QuestionRecord for Multiple Choice Questions.
 *
 * <p>Stored in the child table 'mc_questions' linked by primary key to 'questions'.
 * Contains the @OneToMany relationship to AnswerOptionRecord.</p>
 */
@Entity
@Table(name = "mc_questions")
@DiscriminatorValue("MULTIPLE_CHOICE")
@PrimaryKeyJoinColumn(name = "id")
public class MultipleChoiceQuestionRecord extends QuestionRecord {

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnswerOptionRecord> options = new ArrayList<>();

    public MultipleChoiceQuestionRecord() {
        super();
    }

    public MultipleChoiceQuestionRecord(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                                        UserRecord author, TopicRecord topic, QuestionMetadataRecord metadata) {
        super(id, statement, difficulty, status, author, topic, metadata);
    }

    public List<AnswerOptionRecord> getOptions() {
        return options;
    }

    public void setOptions(List<AnswerOptionRecord> options) {
        this.options.clear();
        if (options != null) {
            for (AnswerOptionRecord opt : options) {
                addOption(opt);
            }
        }
    }

    public void addOption(AnswerOptionRecord option) {
        if (option != null) {
            this.options.add(option);
            option.setQuestion(this);
        }
    }

    public void removeOption(AnswerOptionRecord option) {
        if (option != null) {
            this.options.remove(option);
            option.setQuestion(null);
        }
    }
}
