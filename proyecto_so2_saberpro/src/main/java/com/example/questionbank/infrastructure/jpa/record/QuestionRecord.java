package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Root JPA entity record for the Question persistence hierarchy.
 *
 * <p>Implements JOINED table inheritance strategy as required by the Taller 10 specification.
 * Common question attributes are mapped here; specialized attributes live in child tables.</p>
 */
@Entity
@Table(name = "questions")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "question_type", discriminatorType = DiscriminatorType.STRING)
public abstract class QuestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String statement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestionStatus status;

    /**
     * ManyToOne relationship: Each question has an author; an author can create many questions.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserRecord author;

    /**
     * ManyToOne relationship: Questions can be categorized under an academic topic.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private TopicRecord topic;

    /**
     * OneToOne relationship: Each question has unique complementary metadata.
     * Stored via unique foreign key metadata_id in the questions table.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "metadata_id", unique = true)
    private QuestionMetadataRecord metadata;

    /**
     * ManyToMany relationship: A question can evaluate multiple competencies and vice-versa.
     * Mapped through the join table 'question_competencies'.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "question_competencies",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "competency_id")
    )
    private Set<CompetencyRecord> competencies = new HashSet<>();

    protected QuestionRecord() {
    }

    protected QuestionRecord(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                             UserRecord author, TopicRecord topic, QuestionMetadataRecord metadata) {
        this.id = id;
        this.statement = statement;
        this.difficulty = difficulty;
        this.status = status;
        this.author = author;
        this.topic = topic;
        this.metadata = metadata;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public QuestionStatus getStatus() {
        return status;
    }

    public void setStatus(QuestionStatus status) {
        this.status = status;
    }

    public UserRecord getAuthor() {
        return author;
    }

    public void setAuthor(UserRecord author) {
        this.author = author;
    }

    public TopicRecord getTopic() {
        return topic;
    }

    public void setTopic(TopicRecord topic) {
        this.topic = topic;
    }

    public QuestionMetadataRecord getMetadata() {
        return metadata;
    }

    public void setMetadata(QuestionMetadataRecord metadata) {
        this.metadata = metadata;
    }

    public Set<CompetencyRecord> getCompetencies() {
        return competencies;
    }

    public void setCompetencies(Set<CompetencyRecord> competencies) {
        this.competencies = competencies != null ? competencies : new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionRecord that = (QuestionRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
