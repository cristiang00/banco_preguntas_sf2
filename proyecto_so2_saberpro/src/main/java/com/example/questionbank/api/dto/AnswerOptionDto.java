package com.example.questionbank.api.dto;

public class AnswerOptionDto {
    private Long id;
    private String text;
    private boolean correct;
    private String explanation;

    public AnswerOptionDto() {
    }

    public AnswerOptionDto(Long id, String text, boolean correct, String explanation) {
        this.id = id;
        this.text = text;
        this.correct = correct;
        this.explanation = explanation;
    }

    public AnswerOptionDto(String text, boolean correct) {
        this(null, text, correct, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
