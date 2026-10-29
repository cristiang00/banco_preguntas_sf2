package com.example.questionbank.api.dto;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.TrueFalseQuestion;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QuestionResponseDto {
    private Long id;
    private String type;
    private String statement;
    private Difficulty difficulty;
    private QuestionStatus status;
    private String authorUsername;
    private String topicName;
    private Set<String> competencyCodes;
    private List<AnswerOptionDto> options;
    private Boolean expectedTruthValue;

    public QuestionResponseDto() {
    }

    public static QuestionResponseDto fromDomain(Question question) {
        if (question == null) return null;
        QuestionResponseDto dto = new QuestionResponseDto();
        dto.setId(question.getId());
        dto.setStatement(question.getStatement());
        dto.setDifficulty(question.getDifficulty());
        dto.setStatus(question.getStatus());
        if (question.getAuthor() != null) {
            dto.setAuthorUsername(question.getAuthor().getUsername());
        }
        if (question.getTopic() != null) {
            dto.setTopicName(question.getTopic().getName());
        }
        if (question.getCompetencies() != null) {
            dto.setCompetencyCodes(question.getCompetencies().stream()
                    .map(c -> c.getCode())
                    .collect(Collectors.toSet()));
        }

        if (question instanceof MultipleChoiceQuestion mc) {
            dto.setType("MULTIPLE_CHOICE");
            if (mc.getOptions() != null) {
                dto.setOptions(mc.getOptions().stream()
                        .map(o -> new AnswerOptionDto(o.getId(), o.getText(), o.isCorrect(), o.getExplanation()))
                        .collect(Collectors.toList()));
            }
        } else if (question instanceof TrueFalseQuestion tf) {
            dto.setType("TRUE_FALSE");
            dto.setExpectedTruthValue(tf.isExpectedTruthValue());
        }

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Set<String> getCompetencyCodes() {
        return competencyCodes;
    }

    public void setCompetencyCodes(Set<String> competencyCodes) {
        this.competencyCodes = competencyCodes;
    }

    public List<AnswerOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<AnswerOptionDto> options) {
        this.options = options;
    }

    public Boolean getExpectedTruthValue() {
        return expectedTruthValue;
    }

    public void setExpectedTruthValue(Boolean expectedTruthValue) {
        this.expectedTruthValue = expectedTruthValue;
    }
}
