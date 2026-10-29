package com.example.questionbank.domain;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating Quiz business domain constraints:
 * Specifically verifying that only PUBLISHED questions can be added to Quizzes.
 */
class QuizDomainTest {

    private User teacher;
    private User author;

    @BeforeEach
    void setUp() {
        teacher = new User(1L, "profe_garcia", "Prof. Garcia", "garcia@unicauca.edu.co", Role.TEACHER, true);
        author = new User(2L, "autor_lopez", "Carlos Lopez", "clopez@unicauca.edu.co", Role.AUTHOR, true);
    }

    @Test
    @DisplayName("Should successfully add PUBLISHED question to Quiz")
    void shouldAddPublishedQuestionToQuiz() {
        Quiz quiz = new Quiz("Simulacro Saber Pro 2026-II", "General competencies mock exam", teacher);

        TrueFalseQuestion question = new TrueFalseQuestion("An interface can contain default methods in Java 8+.", Difficulty.EASY, author, true);
        question.setStatus(QuestionStatus.PUBLISHED);

        assertDoesNotThrow(() -> quiz.addQuestion(question));
        assertEquals(1, quiz.getQuestions().size());
    }

    @Test
    @DisplayName("Should reject adding DRAFT question to Quiz with IllegalStateException")
    void shouldRejectDraftQuestion() {
        Quiz quiz = new Quiz("Simulacro Saber Pro", "Mock exam", teacher);

        TrueFalseQuestion draftQuestion = new TrueFalseQuestion("Draft question", Difficulty.EASY, author, true);
        assertEquals(QuestionStatus.DRAFT, draftQuestion.getStatus());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> quiz.addQuestion(draftQuestion));
        assertTrue(ex.getMessage().contains("Only APPROVED and PUBLISHED questions are permitted in quizzes"));
        assertTrue(quiz.getQuestions().isEmpty());
    }

    @Test
    @DisplayName("Should reject adding PENDING_REVIEW or IN_REVIEW question to Quiz")
    void shouldRejectUnderReviewQuestion() {
        Quiz quiz = new Quiz("Simulacro Saber Pro", "Mock exam", teacher);

        TrueFalseQuestion question = new TrueFalseQuestion("Review question", Difficulty.EASY, author, true);
        question.setStatus(QuestionStatus.PENDING_REVIEW);

        assertThrows(IllegalStateException.class, () -> quiz.addQuestion(question));

        question.setStatus(QuestionStatus.IN_REVIEW);
        assertThrows(IllegalStateException.class, () -> quiz.addQuestion(question));
    }

    @Test
    @DisplayName("Should reject adding APPROVED but NOT YET PUBLISHED question to Quiz")
    void shouldRejectApprovedButNotPublishedQuestion() {
        Quiz quiz = new Quiz("Simulacro Saber Pro", "Mock exam", teacher);

        TrueFalseQuestion question = new TrueFalseQuestion("Approved question", Difficulty.EASY, author, true);
        question.setStatus(QuestionStatus.APPROVED);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> quiz.addQuestion(question));
        assertTrue(ex.getMessage().contains("APPROVED"));
    }
}
