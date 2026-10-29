package com.example.questionbank.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing an evaluation/review of a question by a Reviewer.
 */
public class Review {

    private Long id;
    private Long questionId;
    private User reviewer;
    private String feedback;
    private boolean approved;
    private LocalDateTime reviewDate;

    public Review() {
        this.reviewDate = LocalDateTime.now();
    }

    public Review(Long id, Long questionId, User reviewer, String feedback, boolean approved, LocalDateTime reviewDate) {
        this.id = id;
        this.questionId = questionId;
        this.reviewer = reviewer;
        this.feedback = feedback;
        this.approved = approved;
        this.reviewDate = reviewDate != null ? reviewDate : LocalDateTime.now();
    }

    public Review(Long questionId, User reviewer, String feedback, boolean approved) {
        this(null, questionId, reviewer, feedback, approved, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
