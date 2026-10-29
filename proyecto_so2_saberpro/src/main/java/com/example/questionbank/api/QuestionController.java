package com.example.questionbank.api;

import com.example.questionbank.api.dto.AnswerOptionDto;
import com.example.questionbank.api.dto.ClassifyQuestionRequest;
import com.example.questionbank.api.dto.CreateMultipleChoiceQuestionRequest;
import com.example.questionbank.api.dto.CreateTrueFalseQuestionRequest;
import com.example.questionbank.api.dto.QuestionMetadataDto;
import com.example.questionbank.api.dto.QuestionResponseDto;
import com.example.questionbank.api.dto.ReviewQuestionRequest;
import com.example.questionbank.application.QuestionService;
import com.example.questionbank.application.ReviewService;
import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Review;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API Presentation Controller for Question operations.
 *
 * <p>Receives incoming HTTP requests and delegates use case execution to QuestionService.</p>
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final ReviewService reviewService;

    public QuestionController(QuestionService questionService, ReviewService reviewService) {
        this.questionService = questionService;
        this.reviewService = reviewService;
    }

    /**
     * Endpoint for Use Case 1a: Create Multiple Choice Question.
     */
    @PostMapping("/multiple-choice")
    public ResponseEntity<QuestionResponseDto> createMultipleChoiceQuestion(
            @RequestBody CreateMultipleChoiceQuestionRequest request) {
        List<AnswerOption> options = new ArrayList<>();
        if (request.getOptions() != null) {
            for (AnswerOptionDto optDto : request.getOptions()) {
                options.add(new AnswerOption(optDto.getText(), optDto.isCorrect(), optDto.getExplanation()));
            }
        }

        QuestionMetadata metadata = mapMetadata(request.getMetadata());

        MultipleChoiceQuestion question = questionService.createMultipleChoiceQuestion(
                request.getStatement(),
                request.getDifficulty(),
                request.getAuthorId(),
                options,
                metadata
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(QuestionResponseDto.fromDomain(question));
    }

    /**
     * Endpoint for Use Case 1b: Create True/False Question.
     */
    @PostMapping("/true-false")
    public ResponseEntity<QuestionResponseDto> createTrueFalseQuestion(
            @RequestBody CreateTrueFalseQuestionRequest request) {
        QuestionMetadata metadata = mapMetadata(request.getMetadata());

        TrueFalseQuestion question = questionService.createTrueFalseQuestion(
                request.getStatement(),
                request.getDifficulty(),
                request.getAuthorId(),
                request.isExpectedTruthValue(),
                metadata
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(QuestionResponseDto.fromDomain(question));
    }

    /**
     * Endpoint for Use Case 2: Retrieve Question by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> getQuestionById(@PathVariable Long id) {
        return questionService.getQuestionById(id)
                .map(q -> ResponseEntity.ok(QuestionResponseDto.fromDomain(q)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint for Use Case 2: Query questions with filters.
     */
    @GetMapping
    public ResponseEntity<List<QuestionResponseDto>> listQuestions(
            @RequestParam(required = false) QuestionStatus status,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long competencyId) {

        List<Question> questions;
        if (competencyId != null) {
            questions = questionService.getPublishedQuestionsByCompetency(competencyId);
        } else if (status != null) {
            questions = questionService.getQuestionsByStatus(status);
        } else if (authorId != null) {
            questions = questionService.getQuestionsByAuthor(authorId);
        } else {
            questions = questionService.getAllQuestions();
        }

        List<QuestionResponseDto> dtos = questions.stream()
                .map(QuestionResponseDto::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint for Use Case 3: Classify Question.
     */
    @PostMapping("/{id}/classify")
    public ResponseEntity<QuestionResponseDto> classifyQuestion(
            @PathVariable Long id,
            @RequestBody ClassifyQuestionRequest request) {
        Question classified = questionService.classifyQuestion(id, request.getTopicId(), request.getCompetencyIds());
        return ResponseEntity.ok(QuestionResponseDto.fromDomain(classified));
    }

    /**
     * Endpoint for Use Case 4: Send Question to Review.
     */
    @PostMapping("/{id}/send-to-review")
    public ResponseEntity<QuestionResponseDto> sendToReview(@PathVariable Long id) {
        Question question = questionService.sendQuestionToReview(id);
        return ResponseEntity.ok(QuestionResponseDto.fromDomain(question));
    }

    /**
     * Endpoint for Reviewing a question.
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<Review> reviewQuestion(
            @PathVariable Long id,
            @RequestBody ReviewQuestionRequest request) {
        Review review = reviewService.submitReview(id, request.getReviewerId(), request.getFeedback(), request.isApproved());
        return ResponseEntity.ok(review);
    }

    /**
     * Endpoint for Publishing an approved Question.
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<QuestionResponseDto> publishQuestion(@PathVariable Long id) {
        Question published = questionService.publishQuestion(id);
        return ResponseEntity.ok(QuestionResponseDto.fromDomain(published));
    }

    private QuestionMetadata mapMetadata(QuestionMetadataDto dto) {
        if (dto == null) return null;
        return new QuestionMetadata(
                dto.getCognitiveLevel(),
                dto.getEstimatedTimeMinutes(),
                dto.getEditorialNotes()
        );
    }
}
