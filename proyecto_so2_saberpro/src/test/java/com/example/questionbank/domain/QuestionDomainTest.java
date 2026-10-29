package com.example.questionbank.domain;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating pure domain rules, validations, and lifecycle transitions
 * completely decoupled from JPA or Spring context.
 */
class QuestionDomainTest {

    private User author;

    @BeforeEach
    void setUp() {
        author = new User(1L, "jsmith", "John Smith", "jsmith@unicauca.edu.co", Role.AUTHOR, true);
    }

    @Nested
    @DisplayName("Multiple Choice Question Validations")
    class MultipleChoiceTests {

        @Test
        @DisplayName("Should successfully send valid Multiple Choice Question to review")
        void shouldSendValidQuestionToReview() {
            MultipleChoiceQuestion question = new MultipleChoiceQuestion("What is polymorphism in OOP?", Difficulty.MEDIUM, author);
            question.addOption(new AnswerOption("Ability of an object to take many forms", true));
            question.addOption(new AnswerOption("Hiding implementation details", false));
            question.addOption(new AnswerOption("Reusing code via inheritance", false));

            assertEquals(QuestionStatus.DRAFT, question.getStatus());
            question.sendToReview();
            assertEquals(QuestionStatus.PENDING_REVIEW, question.getStatus());
        }

        @Test
        @DisplayName("Should fail when sending to review without options")
        void shouldFailWhenNoOptions() {
            MultipleChoiceQuestion question = new MultipleChoiceQuestion("Valid statement?", Difficulty.MEDIUM, author);

            IllegalStateException ex = assertThrows(IllegalStateException.class, question::sendToReview);
            assertTrue(ex.getMessage().contains("at least 2 answer options"));
        }

        @Test
        @DisplayName("Should fail when sending to review without any correct option")
        void shouldFailWhenNoCorrectOption() {
            MultipleChoiceQuestion question = new MultipleChoiceQuestion("Valid statement?", Difficulty.MEDIUM, author);
            question.addOption(new AnswerOption("Option A", false));
            question.addOption(new AnswerOption("Option B", false));

            IllegalStateException ex = assertThrows(IllegalStateException.class, question::sendToReview);
            assertTrue(ex.getMessage().contains("at least one correct answer option"));
        }

        @Test
        @DisplayName("Should fail when sending to review with blank statement")
        void shouldFailWhenBlankStatement() {
            MultipleChoiceQuestion question = new MultipleChoiceQuestion("   ", Difficulty.MEDIUM, author);
            question.addOption(new AnswerOption("Option A", true));
            question.addOption(new AnswerOption("Option B", false));

            IllegalStateException ex = assertThrows(IllegalStateException.class, question::sendToReview);
            assertTrue(ex.getMessage().contains("statement cannot be null or empty"));
        }

        @Test
        @DisplayName("Should fail when sending to review without author")
        void shouldFailWhenNoAuthor() {
            MultipleChoiceQuestion question = new MultipleChoiceQuestion("Statement", Difficulty.MEDIUM, null);
            question.addOption(new AnswerOption("Option A", true));
            question.addOption(new AnswerOption("Option B", false));

            IllegalStateException ex = assertThrows(IllegalStateException.class, question::sendToReview);
            assertTrue(ex.getMessage().contains("must have an author"));
        }
    }

    @Nested
    @DisplayName("Lifecycle Transitions and Business Rules")
    class LifecycleTests {

        @Test
        @DisplayName("Complete happy path lifecycle: DRAFT -> PENDING_REVIEW -> IN_REVIEW -> APPROVED -> PUBLISHED -> ARCHIVED")
        void testHappyPathLifecycle() {
            TrueFalseQuestion question = new TrueFalseQuestion("Java is statically typed.", Difficulty.EASY, author, true);
            assertEquals(QuestionStatus.DRAFT, question.getStatus());
            assertFalse(question.isEligibleForQuiz());

            // 1. Send to review
            question.sendToReview();
            assertEquals(QuestionStatus.PENDING_REVIEW, question.getStatus());

            // 2. Reviewer starts review
            question.startReview();
            assertEquals(QuestionStatus.IN_REVIEW, question.getStatus());

            // 3. Reviewer approves
            question.approve();
            assertEquals(QuestionStatus.APPROVED, question.getStatus());
            assertFalse(question.isEligibleForQuiz()); // Only PUBLISHED can be in quiz

            // 4. Publish question
            question.publish();
            assertEquals(QuestionStatus.PUBLISHED, question.getStatus());
            assertTrue(question.isEligibleForQuiz());

            // 5. Archive question
            question.archive();
            assertEquals(QuestionStatus.ARCHIVED, question.getStatus());
            assertFalse(question.isEligibleForQuiz());
        }

        @Test
        @DisplayName("Rejecting a question returns it to REJECTED for correction")
        void testRejectLifecycle() {
            TrueFalseQuestion question = new TrueFalseQuestion("Statement", Difficulty.EASY, author, true);
            question.sendToReview();
            question.startReview();

            question.reject("Grammar error in statement");
            assertEquals(QuestionStatus.REJECTED, question.getStatus());

            // Can be corrected and re-submitted
            question.setStatement("Corrected statement");
            question.sendToReview();
            assertEquals(QuestionStatus.PENDING_REVIEW, question.getStatus());
        }

        @Test
        @DisplayName("Cannot publish question directly from DRAFT or PENDING_REVIEW")
        void testInvalidPublishTransition() {
            TrueFalseQuestion question = new TrueFalseQuestion("Statement", Difficulty.EASY, author, true);
            assertThrows(IllegalStateException.class, question::publish);

            question.sendToReview();
            assertThrows(IllegalStateException.class, question::publish);
        }
    }
}
