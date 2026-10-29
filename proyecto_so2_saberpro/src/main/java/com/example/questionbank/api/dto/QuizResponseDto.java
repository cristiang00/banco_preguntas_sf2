package com.example.questionbank.api.dto;

import com.example.questionbank.domain.model.Quiz;

import java.util.List;
import java.util.stream.Collectors;

public class QuizResponseDto {
    private Long id;
    private String title;
    private String description;
    private String createdByUsername;
    private Integer totalScore;
    private List<QuestionResponseDto> questions;

    public QuizResponseDto() {
    }

    public static QuizResponseDto fromDomain(Quiz quiz) {
        if (quiz == null) return null;
        QuizResponseDto dto = new QuizResponseDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        if (quiz.getCreatedBy() != null) {
            dto.setCreatedByUsername(quiz.getCreatedBy().getUsername());
        }
        dto.setTotalScore(quiz.getTotalScore());
        if (quiz.getQuestions() != null) {
            dto.setQuestions(quiz.getQuestions().stream()
                    .map(QuestionResponseDto::fromDomain)
                    .collect(Collectors.toList()));
        }
        return dto;
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

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public List<QuestionResponseDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponseDto> questions) {
        this.questions = questions;
    }
}
