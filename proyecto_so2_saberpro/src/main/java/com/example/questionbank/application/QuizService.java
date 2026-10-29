package com.example.questionbank.application;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.QuizRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service coordinating Quiz and Mock Exam creation use cases.
 *
 * <p>Enforces the rule that only approved and published questions can be used in quizzes.</p>
 */
@Service
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public QuizService(QuizRepository quizRepository,
                       QuestionRepository questionRepository,
                       UserRepository userRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Use Case 5: Create a Quiz / Mock Exam.
     * Guarantees that only published questions are incorporated into the quiz.
     *
     * @param title        quiz title
     * @param description  quiz description
     * @param createdById  teacher or admin ID creating the quiz
     * @param questionIds  list of question identifiers to associate
     * @param totalScore   total score
     * @return persisted Quiz entity
     */
    public Quiz createQuiz(String title, String description, Long createdById, List<Long> questionIds, Integer totalScore) {
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + createdById));

        Quiz quiz = new Quiz(title, description, createdBy);
        if (totalScore != null) {
            quiz.setTotalScore(totalScore);
        }

        if (questionIds != null && !questionIds.isEmpty()) {
            for (Long qId : questionIds) {
                Question question = questionRepository.findById(qId)
                        .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + qId));
                // addQuestion validates that question.isEligibleForQuiz() == true (status == PUBLISHED)
                quiz.addQuestion(question);
            }
        }

        return quizRepository.save(quiz);
    }

    @Transactional(readOnly = true)
    public Optional<Quiz> getQuizById(Long id) {
        return quizRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }
}
