package com.example.questionbank.api;

import com.example.questionbank.api.dto.CreateQuizRequest;
import com.example.questionbank.api.dto.QuizResponseDto;
import com.example.questionbank.application.QuizService;
import com.example.questionbank.domain.model.Quiz;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API Presentation Controller for Quiz/Mock Exam operations.
 *
 * <p>Receives incoming requests for quiz creation and retrieval, delegating to QuizService.</p>
 */
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * Endpoint for Use Case 5: Create a Quiz with published questions.
     */
    @PostMapping
    public ResponseEntity<QuizResponseDto> createQuiz(@RequestBody CreateQuizRequest request) {
        Quiz quiz = quizService.createQuiz(
                request.getTitle(),
                request.getDescription(),
                request.getCreatedById(),
                request.getQuestionIds(),
                request.getTotalScore()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(QuizResponseDto.fromDomain(quiz));
    }

    /**
     * Endpoint for retrieving a Quiz by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuizResponseDto> getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id)
                .map(q -> ResponseEntity.ok(QuizResponseDto.fromDomain(q)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint for listing all Quizzes.
     */
    @GetMapping
    public ResponseEntity<List<QuizResponseDto>> listQuizzes() {
        List<QuizResponseDto> dtos = quizService.getAllQuizzes().stream()
                .map(QuizResponseDto::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Handles business rule violations when attempting to add unapproved questions to a Quiz.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRuleViolation(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "BUSINESS_RULE_VIOLATION", "message", ex.getMessage()));
    }
}
