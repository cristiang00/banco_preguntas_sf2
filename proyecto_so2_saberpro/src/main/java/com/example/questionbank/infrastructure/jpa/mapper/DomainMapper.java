package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Review;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.infrastructure.jpa.record.AnswerOptionRecord;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.record.MultipleChoiceQuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionMetadataRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import com.example.questionbank.infrastructure.jpa.record.ReviewRecord;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.record.TrueFalseQuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Component responsible for bidirectional translation between pure domain entities
 * and JPA entity records.
 *
 * <p>Ensures that the domain layer remains 100% free of persistence annotations and concepts.</p>
 */
@Component
public class DomainMapper {

    // --------------------------------------------------------------------------
    // User Mapping
    // --------------------------------------------------------------------------

    public User toDomain(UserRecord record) {
        if (record == null) return null;
        return new User(
                record.getId(),
                record.getUsername(),
                record.getFullName(),
                record.getEmail(),
                record.getRole(),
                record.isActive()
        );
    }

    public UserRecord toRecord(User domain) {
        if (domain == null) return null;
        return new UserRecord(
                domain.getId(),
                domain.getUsername(),
                domain.getFullName(),
                domain.getEmail(),
                domain.getRole(),
                domain.isActive()
        );
    }

    // --------------------------------------------------------------------------
    // Competency Mapping
    // --------------------------------------------------------------------------

    public Competency toDomain(CompetencyRecord record) {
        if (record == null) return null;
        return new Competency(
                record.getId(),
                record.getCode(),
                record.getName(),
                record.getDescription()
        );
    }

    public CompetencyRecord toRecord(Competency domain) {
        if (domain == null) return null;
        return new CompetencyRecord(
                domain.getId(),
                domain.getCode(),
                domain.getName(),
                domain.getDescription()
        );
    }

    // --------------------------------------------------------------------------
    // Topic Mapping
    // --------------------------------------------------------------------------

    public Topic toDomain(TopicRecord record) {
        if (record == null) return null;
        return new Topic(
                record.getId(),
                record.getName(),
                record.getDescription()
        );
    }

    public TopicRecord toRecord(Topic domain) {
        if (domain == null) return null;
        return new TopicRecord(
                domain.getId(),
                domain.getName(),
                domain.getDescription()
        );
    }

    // --------------------------------------------------------------------------
    // QuestionMetadata Mapping
    // --------------------------------------------------------------------------

    public QuestionMetadata toDomain(QuestionMetadataRecord record) {
        if (record == null) return null;
        return new QuestionMetadata(
                record.getId(),
                record.getLastReviewDate(),
                record.getCognitiveLevel(),
                record.getEstimatedTimeMinutes(),
                record.getEditorialNotes()
        );
    }

    public QuestionMetadataRecord toRecord(QuestionMetadata domain) {
        if (domain == null) return null;
        return new QuestionMetadataRecord(
                domain.getId(),
                domain.getLastReviewDate(),
                domain.getCognitiveLevel(),
                domain.getEstimatedTimeMinutes(),
                domain.getEditorialNotes()
        );
    }

    // --------------------------------------------------------------------------
    // AnswerOption Mapping
    // --------------------------------------------------------------------------

    public AnswerOption toDomain(AnswerOptionRecord record) {
        if (record == null) return null;
        return new AnswerOption(
                record.getId(),
                record.getText(),
                record.isCorrect(),
                record.getExplanation()
        );
    }

    public AnswerOptionRecord toRecord(AnswerOption domain) {
        if (domain == null) return null;
        return new AnswerOptionRecord(
                domain.getId(),
                domain.getText(),
                domain.isCorrect(),
                domain.getExplanation()
        );
    }

    // --------------------------------------------------------------------------
    // Question Hierarchy Mapping (JOINED Inheritance)
    // --------------------------------------------------------------------------

    public Question toDomain(QuestionRecord record) {
        if (record == null) return null;

        User author = toDomain(record.getAuthor());
        Topic topic = toDomain(record.getTopic());
        QuestionMetadata metadata = toDomain(record.getMetadata());
        Set<Competency> competencies = record.getCompetencies() != null
                ? record.getCompetencies().stream().map(this::toDomain).collect(Collectors.toSet())
                : new HashSet<>();

        if (record instanceof MultipleChoiceQuestionRecord mcRecord) {
            List<AnswerOption> options = mcRecord.getOptions() != null
                    ? mcRecord.getOptions().stream().map(this::toDomain).collect(Collectors.toList())
                    : new ArrayList<>();
            return new MultipleChoiceQuestion(
                    record.getId(),
                    record.getStatement(),
                    record.getDifficulty(),
                    record.getStatus(),
                    author,
                    topic,
                    competencies,
                    metadata,
                    options
            );
        } else if (record instanceof TrueFalseQuestionRecord tfRecord) {
            return new TrueFalseQuestion(
                    record.getId(),
                    record.getStatement(),
                    record.getDifficulty(),
                    record.getStatus(),
                    author,
                    topic,
                    competencies,
                    metadata,
                    tfRecord.isExpectedTruthValue()
            );
        } else {
            throw new IllegalArgumentException("Unsupported QuestionRecord subclass: " + record.getClass().getName());
        }
    }

    public QuestionRecord toRecord(Question domain) {
        if (domain == null) return null;

        UserRecord authorRecord = toRecord(domain.getAuthor());
        TopicRecord topicRecord = toRecord(domain.getTopic());
        QuestionMetadataRecord metadataRecord = toRecord(domain.getMetadata());
        Set<CompetencyRecord> competencyRecords = domain.getCompetencies() != null
                ? domain.getCompetencies().stream().map(this::toRecord).collect(Collectors.toSet())
                : new HashSet<>();

        if (domain instanceof MultipleChoiceQuestion mcDomain) {
            MultipleChoiceQuestionRecord mcRecord = new MultipleChoiceQuestionRecord(
                    domain.getId(),
                    domain.getStatement(),
                    domain.getDifficulty(),
                    domain.getStatus(),
                    authorRecord,
                    topicRecord,
                    metadataRecord
            );
            mcRecord.setCompetencies(competencyRecords);
            if (mcDomain.getOptions() != null) {
                for (AnswerOption opt : mcDomain.getOptions()) {
                    AnswerOptionRecord optRecord = toRecord(opt);
                    mcRecord.addOption(optRecord);
                }
            }
            return mcRecord;
        } else if (domain instanceof TrueFalseQuestion tfDomain) {
            TrueFalseQuestionRecord tfRecord = new TrueFalseQuestionRecord(
                    domain.getId(),
                    domain.getStatement(),
                    domain.getDifficulty(),
                    domain.getStatus(),
                    authorRecord,
                    topicRecord,
                    metadataRecord,
                    tfDomain.isExpectedTruthValue()
            );
            tfRecord.setCompetencies(competencyRecords);
            return tfRecord;
        } else {
            throw new IllegalArgumentException("Unsupported Question subclass: " + domain.getClass().getName());
        }
    }

    // --------------------------------------------------------------------------
    // Review Mapping
    // --------------------------------------------------------------------------

    public Review toDomain(ReviewRecord record) {
        if (record == null) return null;
        return new Review(
                record.getId(),
                record.getQuestionId(),
                toDomain(record.getReviewer()),
                record.getFeedback(),
                record.isApproved(),
                record.getReviewDate()
        );
    }

    public ReviewRecord toRecord(Review domain) {
        if (domain == null) return null;
        return new ReviewRecord(
                domain.getId(),
                domain.getQuestionId(),
                toRecord(domain.getReviewer()),
                domain.getFeedback(),
                domain.isApproved(),
                domain.getReviewDate()
        );
    }

    // --------------------------------------------------------------------------
    // Quiz Mapping
    // --------------------------------------------------------------------------

    public Quiz toDomain(QuizRecord record) {
        if (record == null) return null;
        Quiz quiz = new Quiz(
                record.getId(),
                record.getTitle(),
                record.getDescription(),
                toDomain(record.getCreatedBy()),
                null,
                record.getTotalScore()
        );
        if (record.getQuestions() != null) {
            for (QuestionRecord qr : record.getQuestions()) {
                quiz.addQuestion(toDomain(qr));
            }
        }
        return quiz;
    }

    public QuizRecord toRecord(Quiz domain) {
        if (domain == null) return null;
        QuizRecord record = new QuizRecord(
                domain.getId(),
                domain.getTitle(),
                domain.getDescription(),
                toRecord(domain.getCreatedBy()),
                domain.getTotalScore()
        );
        if (domain.getQuestions() != null) {
            record.setQuestions(domain.getQuestions().stream()
                    .map(this::toRecord)
                    .collect(Collectors.toList()));
        }
        return record;
    }
}
