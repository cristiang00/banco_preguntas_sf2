package com.example.questionbank.application;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.CognitiveLevel;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Review;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests verifying all 5 use cases defined in Punto 8 of Taller 10.
 */
@SpringBootTest
@Transactional
class QuestionBankUseCasesIntegrationTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private TopicRepository topicRepository;

    private User author;
    private User reviewer;
    private User teacher;
    private Topic topic;
    private Competency compSoftware;
    private Competency compAlgo;

    @BeforeEach
    void setUp() {
        author = userRepository.save(new User("author_uc", "Carlos Autor", "carlos@unicauca.edu.co", Role.AUTHOR));
        reviewer = userRepository.save(new User("reviewer_uc", "Ana Revisora", "ana@unicauca.edu.co", Role.REVIEWER));
        teacher = userRepository.save(new User("teacher_uc", "Profe Docente", "docente@unicauca.edu.co", Role.TEACHER));

        topic = topicRepository.save(new Topic("Clean Architecture", "Layered and hexagonal design principles"));
        compSoftware = competencyRepository.save(new Competency("COMP-SW", "Software Engineering", "Core engineering principles"));
        compAlgo = competencyRepository.save(new Competency("COMP-ALGO", "Algorithms", "Data structures and algorithm design"));
    }

    @Test
    @DisplayName("Use Case 1: Create Multiple Choice and True/False questions with options and metadata")
    void testUseCase1_CreateQuestions() {
        // 1a. Multiple Choice
        List<AnswerOption> options = List.of(
                new AnswerOption("Encapsulation", true, "Hides internal object state"),
                new AnswerOption("Aggregation", false, "Specialized form of association"),
                new AnswerOption("Coupling", false, "Degree of interdependence"),
                new AnswerOption("Compilation", false, "Translation to bytecode")
        );
        QuestionMetadata metadata = new QuestionMetadata(CognitiveLevel.UNDERSTAND, 10, "Focus on OOP pillars.");

        MultipleChoiceQuestion mc = questionService.createMultipleChoiceQuestion(
                "Which OOP principle hides internal representation?",
                Difficulty.MEDIUM,
                author.getId(),
                options,
                metadata
        );

        assertNotNull(mc.getId());
        assertEquals(QuestionStatus.DRAFT, mc.getStatus());
        assertEquals(4, mc.getOptions().size());
        assertEquals(1, mc.getCorrectOptions().size());

        // 1b. True/False
        TrueFalseQuestion tf = questionService.createTrueFalseQuestion(
                "JPA entity annotations should never be placed directly inside domain models.",
                Difficulty.EASY,
                author.getId(),
                true,
                new QuestionMetadata(CognitiveLevel.APPLY, 5, "Architectural boundary rule.")
        );

        assertNotNull(tf.getId());
        assertEquals(QuestionStatus.DRAFT, tf.getStatus());
        assertTrue(tf.isExpectedTruthValue());
    }

    @Test
    @DisplayName("Use Case 2: Query questions by ID and filter by author, status, and competency")
    void testUseCase2_QueryQuestions() {
        TrueFalseQuestion q1 = questionService.createTrueFalseQuestion("Q1 statement", Difficulty.EASY, author.getId(), true, null);
        TrueFalseQuestion q2 = questionService.createTrueFalseQuestion("Q2 statement", Difficulty.MEDIUM, author.getId(), false, null);

        // Query by ID
        assertTrue(questionService.getQuestionById(q1.getId()).isPresent());

        // Filter by Author
        List<Question> authorQuestions = questionService.getQuestionsByAuthor(author.getId());
        assertEquals(2, authorQuestions.size());

        // Filter by Status
        List<Question> draftQuestions = questionService.getQuestionsByStatus(QuestionStatus.DRAFT);
        assertTrue(draftQuestions.size() >= 2);

        // Filter published by competency
        questionService.classifyQuestion(q1.getId(), topic.getId(), Set.of(compSoftware.getId()));
        questionService.sendQuestionToReview(q1.getId());
        reviewService.submitReview(q1.getId(), reviewer.getId(), "Approved", true);
        questionService.publishQuestion(q1.getId());

        List<Question> compQuestions = questionService.getPublishedQuestionsByCompetency(compSoftware.getId());
        assertEquals(1, compQuestions.size());
        assertEquals(q1.getId(), compQuestions.get(0).getId());
    }

    @Test
    @DisplayName("Use Case 3: Classify a question by linking it to a topic and competencies")
    void testUseCase3_ClassifyQuestion() {
        TrueFalseQuestion question = questionService.createTrueFalseQuestion("Statement to classify", Difficulty.MEDIUM, author.getId(), true, null);

        Question classified = questionService.classifyQuestion(
                question.getId(),
                topic.getId(),
                Set.of(compSoftware.getId(), compAlgo.getId())
        );

        assertNotNull(classified.getTopic());
        assertEquals("Clean Architecture", classified.getTopic().getName());
        assertEquals(2, classified.getCompetencies().size());
    }

    @Test
    @DisplayName("Use Case 4: Send question to review and execute review workflow")
    void testUseCase4_SendToReviewAndReviewCycle() {
        List<AnswerOption> options = List.of(
                new AnswerOption("Correct option", true),
                new AnswerOption("Distractor option", false)
        );
        MultipleChoiceQuestion question = questionService.createMultipleChoiceQuestion("Review question?", Difficulty.EASY, author.getId(), options, null);

        // Send to review
        Question inPending = questionService.sendQuestionToReview(question.getId());
        assertEquals(QuestionStatus.PENDING_REVIEW, inPending.getStatus());

        // Reviewer approves question
        Review review = reviewService.submitReview(question.getId(), reviewer.getId(), "Clear and concise.", true);
        assertNotNull(review.getId());
        assertTrue(review.isApproved());

        Question approved = questionService.getQuestionById(question.getId()).orElseThrow();
        assertEquals(QuestionStatus.APPROVED, approved.getStatus());

        // Author/Admin publishes
        Question published = questionService.publishQuestion(question.getId());
        assertEquals(QuestionStatus.PUBLISHED, published.getStatus());
    }

    @Test
    @DisplayName("Use Case 5 (Success): Create Quiz associating only PUBLISHED questions")
    void testUseCase5_CreateQuizWithPublishedQuestions() {
        // Setup 2 published questions
        TrueFalseQuestion q1 = questionService.createTrueFalseQuestion("Q1 for quiz", Difficulty.EASY, author.getId(), true, null);
        questionService.sendQuestionToReview(q1.getId());
        reviewService.submitReview(q1.getId(), reviewer.getId(), "Good", true);
        questionService.publishQuestion(q1.getId());

        TrueFalseQuestion q2 = questionService.createTrueFalseQuestion("Q2 for quiz", Difficulty.MEDIUM, author.getId(), false, null);
        questionService.sendQuestionToReview(q2.getId());
        reviewService.submitReview(q2.getId(), reviewer.getId(), "Good", true);
        questionService.publishQuestion(q2.getId());

        Quiz quiz = quizService.createQuiz(
                "Saber Pro Simulacro 1",
                "Practice simulation for upcoming test",
                teacher.getId(),
                List.of(q1.getId(), q2.getId()),
                100
        );

        assertNotNull(quiz.getId());
        assertEquals(2, quiz.getQuestions().size());
        assertEquals("Saber Pro Simulacro 1", quiz.getTitle());
    }

    @Test
    @DisplayName("Use Case 5 (Error): Reject Quiz creation if any question is still in DRAFT or not PUBLISHED")
    void testUseCase5_RejectQuizCreationWithUnpublishedQuestion() {
        // 1 published question
        TrueFalseQuestion published = questionService.createTrueFalseQuestion("Published Question", Difficulty.EASY, author.getId(), true, null);
        questionService.sendQuestionToReview(published.getId());
        reviewService.submitReview(published.getId(), reviewer.getId(), "Good", true);
        questionService.publishQuestion(published.getId());

        // 1 draft question
        TrueFalseQuestion draft = questionService.createTrueFalseQuestion("Draft Question", Difficulty.EASY, author.getId(), false, null);

        // Attempting to create a Quiz containing the draft question must fail with IllegalStateException
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                quizService.createQuiz(
                        "Illegal Quiz",
                        "Attempt to use unpublished questions",
                        teacher.getId(),
                        List.of(published.getId(), draft.getId()),
                        100
                )
        );

        assertTrue(ex.getMessage().contains("Only APPROVED and PUBLISHED questions are permitted in quizzes"));
    }
}
