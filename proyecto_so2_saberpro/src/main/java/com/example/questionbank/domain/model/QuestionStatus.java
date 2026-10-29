package com.example.questionbank.domain.model;

/**
 * Enumeration representing the complete lifecycle states of a Question.
 *
 * <p>Lifecycle flow:
 * DRAFT -> PENDING_REVIEW -> IN_REVIEW -> APPROVED -> PUBLISHED -> ARCHIVED
 * A question can also be rejected and returned to DRAFT for corrections.</p>
 */
public enum QuestionStatus {
    DRAFT,
    PENDING_REVIEW,
    IN_REVIEW,
    APPROVED,
    PUBLISHED,
    ARCHIVED,
    REJECTED
}
