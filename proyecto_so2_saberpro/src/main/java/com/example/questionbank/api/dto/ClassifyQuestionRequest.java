package com.example.questionbank.api.dto;

import java.util.Set;

public class ClassifyQuestionRequest {
    private Long topicId;
    private Set<Long> competencyIds;

    public ClassifyQuestionRequest() {
    }

    public ClassifyQuestionRequest(Long topicId, Set<Long> competencyIds) {
        this.topicId = topicId;
        this.competencyIds = competencyIds;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Set<Long> getCompetencyIds() {
        return competencyIds;
    }

    public void setCompetencyIds(Set<Long> competencyIds) {
        this.competencyIds = competencyIds;
    }
}
