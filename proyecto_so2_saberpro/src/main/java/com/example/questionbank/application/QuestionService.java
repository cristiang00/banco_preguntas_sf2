package com.example.questionbank.application;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Application service coordinating use cases for Question management.
 *
 * <p>Strictly depends on domain repository interfaces, not on Spring Data JpaRepository,
 * adhering to the Clean Persistence and Layered Architecture requirements.</p>
 */
@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CompetencyRepository competencyRepository;
    private final TopicRepository topicRepository;

    public QuestionService(QuestionRepository questionRepository,
                           UserRepository userRepository,
                           CompetencyRepository competencyRepository,
                           TopicRepository topicRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.competencyRepository = competencyRepository;
        this.topicRepository = topicRepository;
    }

    /**
     * Use Case 1a: Create a Multiple Choice Question.
     */
    public MultipleChoiceQuestion createMultipleChoiceQuestion(String statement, Difficulty difficulty, Long authorId,
                                                               List<AnswerOption> options, QuestionMetadata metadata) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with ID: " + authorId));

        MultipleChoiceQuestion question = new MultipleChoiceQuestion(statement, difficulty, author);
        question.setMetadata(metadata);
        if (options != null) {
            for (AnswerOption option : options) {
                question.addOption(option);
            }
        }
        return (MultipleChoiceQuestion) questionRepository.save(question);
    }

    /**
     * Use Case 1b: Create a True/False Question.
     */
    public TrueFalseQuestion createTrueFalseQuestion(String statement, Difficulty difficulty, Long authorId,
                                                     boolean expectedTruthValue, QuestionMetadata metadata) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with ID: " + authorId));

        TrueFalseQuestion question = new TrueFalseQuestion(statement, difficulty, author, expectedTruthValue);
        question.setMetadata(metadata);
        return (TrueFalseQuestion) questionRepository.save(question);
    }

    /**
     * Use Case 2: Retrieve questions by ID, or list according to criteria.
     */
    @Transactional(readOnly = true)
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Question> getQuestionsByStatus(QuestionStatus status) {
        return questionRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Question> getQuestionsByAuthor(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with ID: " + authorId));
        return questionRepository.findByAuthor(author);
    }

    @Transactional(readOnly = true)
    public List<Question> getPublishedQuestionsByCompetency(Long competencyId) {
        return questionRepository.findPublishedByCompetency(competencyId);
    }

    /**
     * Use Case 3: Classify a question by linking it to a topic and one or more competencies.
     */
    public Question classifyQuestion(Long questionId, Long topicId, Set<Long> competencyIds) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        if (topicId != null) {
            Topic topic = topicRepository.findById(topicId)
                    .orElseThrow(() -> new IllegalArgumentException("Topic not found with ID: " + topicId));
            question.setTopic(topic);
        }

        if (competencyIds != null && !competencyIds.isEmpty()) {
            Set<Competency> competencies = new HashSet<>();
            for (Long compId : competencyIds) {
                Competency comp = competencyRepository.findById(compId)
                        .orElseThrow(() -> new IllegalArgumentException("Competency not found with ID: " + compId));
                competencies.add(comp);
            }
            question.setCompetencies(competencies);
        }

        return questionRepository.save(question);
    }

    /**
     * Use Case 4: Send a question to review.
     * Validates business rules to advance state from DRAFT to PENDING_REVIEW.
     */
    public Question sendQuestionToReview(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        question.sendToReview();
        return questionRepository.save(question);
    }

    /**
     * Advances question state to IN_REVIEW.
     */
    public Question startReview(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        question.startReview();
        return questionRepository.save(question);
    }

    /**
     * Approves a question after successful evaluation.
     */
    public Question approveQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        question.approve();
        return questionRepository.save(question);
    }

    /**
     * Rejects a question with feedback, returning it to DRAFT/REJECTED.
     */
    public Question rejectQuestion(Long questionId, String feedback) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        question.reject(feedback);
        return questionRepository.save(question);
    }

    /**
     * Publishes an approved question making it eligible for quizzes.
     */
    public Question publishQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        question.publish();
        return questionRepository.save(question);
    }

    /**
     * Archives a question.
     */
    public Question archiveQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        question.archive();
        return questionRepository.save(question);
    }
}
