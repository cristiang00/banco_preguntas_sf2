package com.example.questionbank.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract domain entity representing a base Question in the Saber Pro bank.
 *
 * <p>Encapsulates core business rules, question attributes, and lifecycle state transitions.
 * Strictly decoupled from persistence technology.</p>
 */
public abstract class Question {

    private Long id;
    private String statement;
    private Difficulty difficulty;
    private QuestionStatus status;
    private User author;
    private Topic topic;
    private Set<Competency> competencies = new HashSet<>();
    private QuestionMetadata metadata;

    protected Question() {
        this.status = QuestionStatus.DRAFT;
        this.difficulty = Difficulty.MEDIUM;
    }

    protected Question(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                       User author, Topic topic, Set<Competency> competencies, QuestionMetadata metadata) {
        this.id = id;
        this.statement = statement;
        this.difficulty = difficulty != null ? difficulty : Difficulty.MEDIUM;
        this.status = status != null ? status : QuestionStatus.DRAFT;
        this.author = author;
        this.topic = topic;
        if (competencies != null) {
            this.competencies.addAll(competencies);
        }
        this.metadata = metadata;
    }

    /**
     * Validates domain constraints before transitioning from DRAFT to PENDING_REVIEW.
     * Subclasses must provide their specific structural validations.
     */
    public abstract void validateForReview();

    /**
     * Submits a question to review, transitioning its state from DRAFT to PENDING_REVIEW.
     *
     * @throws IllegalStateException if preconditions are not met or transition is invalid
     */
    public void sendToReview() {
        if (this.status != QuestionStatus.DRAFT && this.status != QuestionStatus.REJECTED) {
            throw new IllegalStateException("Only questions in DRAFT or REJECTED status can be sent to review. Current: " + this.status);
        }
        if (this.statement == null || this.statement.trim().isEmpty()) {
            throw new IllegalStateException("Question statement cannot be null or empty.");
        }
        if (this.author == null) {
            throw new IllegalStateException("Question must have an author assigned before sending to review.");
        }
        if (this.difficulty == null) {
            throw new IllegalStateException("Question must have a difficulty level assigned.");
        }

        // Validate subclass specific rules
        validateForReview();

        this.status = QuestionStatus.PENDING_REVIEW;
    }

    /**
     * Starts formal review of the question by an assigned reviewer.
     */
    public void startReview() {
        if (this.status != QuestionStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Cannot start review from status: " + this.status + ". Must be PENDING_REVIEW.");
        }
        this.status = QuestionStatus.IN_REVIEW;
    }

    /**
     * Approves the question after a successful review.
     */
    public void approve() {
        if (this.status != QuestionStatus.IN_REVIEW && this.status != QuestionStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Cannot approve question with status: " + this.status);
        }
        this.status = QuestionStatus.APPROVED;
        if (this.metadata != null) {
            this.metadata.setLastReviewDate(LocalDateTime.now());
        }
    }

    /**
     * Rejects the question with feedback, returning it to DRAFT for author corrections.
     *
     * @param feedback reason for rejection
     */
    public void reject(String feedback) {
        if (this.status != QuestionStatus.IN_REVIEW && this.status != QuestionStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Cannot reject question with status: " + this.status);
        }
        this.status = QuestionStatus.REJECTED;
        if (this.metadata != null && feedback != null) {
            String current = this.metadata.getEditorialNotes();
            this.metadata.setEditorialNotes((current != null ? current + " | " : "") + "Rejected: " + feedback);
        }
    }

    /**
     * Publishes an approved question making it available for quizzes/mock exams.
     */
    public void publish() {
        if (this.status != QuestionStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED questions can be published. Current status: " + this.status);
        }
        this.status = QuestionStatus.PUBLISHED;
    }

    /**
     * Archives a question retiring it from active use.
     */
    public void archive() {
        this.status = QuestionStatus.ARCHIVED;
    }

    /**
     * Checks if the question is eligible to be included in an exam/quiz.
     *
     * @return true if status is PUBLISHED, false otherwise
     */
    public boolean isEligibleForQuiz() {
        return this.status == QuestionStatus.PUBLISHED;
    }

    public void addCompetency(Competency competency) {
        if (competency != null) {
            this.competencies.add(competency);
        }
    }

    public void removeCompetency(Competency competency) {
        this.competencies.remove(competency);
    }

    // Getters and Setters

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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Set<Competency> getCompetencies() {
        return competencies;
    }

    public void setCompetencies(Set<Competency> competencies) {
        this.competencies = competencies != null ? competencies : new HashSet<>();
    }

    public QuestionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(QuestionMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return Objects.equals(id, question.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", statement='" + statement + '\'' +
                ", status=" + status +
                ", difficulty=" + difficulty +
                '}';
    }
}
