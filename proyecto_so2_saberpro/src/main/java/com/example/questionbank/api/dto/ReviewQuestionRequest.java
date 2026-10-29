package com.example.questionbank.api.dto;

public class ReviewQuestionRequest {
    private Long reviewerId;
    private String feedback;
    private boolean approved;

    public ReviewQuestionRequest() {
    }

    public ReviewQuestionRequest(Long reviewerId, String feedback, boolean approved) {
        this.reviewerId = reviewerId;
        this.feedback = feedback;
        this.approved = approved;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
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
}
