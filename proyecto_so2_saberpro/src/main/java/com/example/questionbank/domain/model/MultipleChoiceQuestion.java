package com.example.questionbank.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain entity representing a Multiple Choice Question (single or multiple correct answers).
 *
 * <p>Contains answer options, distractors, and validation rules.</p>
 */
public class MultipleChoiceQuestion extends Question {

    private List<AnswerOption> options = new ArrayList<>();

    public MultipleChoiceQuestion() {
        super();
    }

    public MultipleChoiceQuestion(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                                  User author, Topic topic, Set<Competency> competencies, QuestionMetadata metadata,
                                  List<AnswerOption> options) {
        super(id, statement, difficulty, status, author, topic, competencies, metadata);
        if (options != null) {
            this.options.addAll(options);
        }
    }

    public MultipleChoiceQuestion(String statement, Difficulty difficulty, User author) {
        super(null, statement, difficulty, QuestionStatus.DRAFT, author, null, null, null);
    }

    @Override
    public void validateForReview() {
        if (options == null || options.size() < 2) {
            throw new IllegalStateException("Multiple choice questions must have at least 2 answer options to be reviewed.");
        }
        boolean hasCorrectOption = options.stream().anyMatch(AnswerOption::isCorrect);
        if (!hasCorrectOption) {
            throw new IllegalStateException("Multiple choice questions must have at least one correct answer option.");
        }
    }

    public void addOption(AnswerOption option) {
        if (option != null) {
            this.options.add(option);
        }
    }

    public void removeOption(AnswerOption option) {
        this.options.remove(option);
    }

    public List<AnswerOption> getCorrectOptions() {
        return options.stream()
                .filter(AnswerOption::isCorrect)
                .collect(Collectors.toList());
    }

    public List<AnswerOption> getOptions() {
        return options;
    }

    public void setOptions(List<AnswerOption> options) {
        this.options = options != null ? options : new ArrayList<>();
    }
}
