package com.example.questionbank.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing an Evaluation Quiz or Mock Exam (simulacro Saber Pro).
 *
 * <p>Enforces the critical business rule:
 * Only APPROVED and PUBLISHED questions can be added to a Quiz.
 * Incorporating draft or unapproved questions is strictly prohibited by domain validation.</p>
 */
public class Quiz {

    private Long id;
    private String title;
    private String description;
    private User createdBy;
    private List<Question> questions = new ArrayList<>();
    private Integer totalScore;

    public Quiz() {
    }

    public Quiz(Long id, String title, String description, User createdBy, List<Question> questions, Integer totalScore) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        if (questions != null) {
            for (Question q : questions) {
                addQuestion(q);
            }
        }
        this.totalScore = totalScore;
    }

    public Quiz(String title, String description, User createdBy) {
        this(null, title, description, createdBy, null, 100);
    }

    /**
     * Adds a question to this quiz, verifying that the question is published.
     *
     * @param question question to include
     * @throws IllegalStateException if question is not in PUBLISHED status
     */
    public void addQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Cannot add a null question to quiz.");
        }
        if (!question.isEligibleForQuiz()) {
            throw new IllegalStateException("Cannot add question with ID " + question.getId() +
                    " to quiz: question is in status '" + question.getStatus() +
                    "'. Only APPROVED and PUBLISHED questions are permitted in quizzes.");
        }
        this.questions.add(question);
    }

    public void removeQuestion(Question question) {
        this.questions.remove(question);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions.clear();
        if (questions != null) {
            for (Question q : questions) {
                addQuestion(q);
            }
        }
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quiz quiz = (Quiz) o;
        return Objects.equals(id, quiz.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", questionCount=" + questions.size() +
                '}';
    }
}
