package com.example.questionbank.application;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Review;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.ReviewRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service coordinating Question Review use cases.
 */
@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         QuestionRepository questionRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Submits a formal review for a question, approving or rejecting it.
     *
     * @param questionId identifier of the question under review
     * @param reviewerId identifier of the reviewer
     * @param feedback   detailed feedback/critique
     * @param approved   true if approved, false if rejected
     * @return saved Review entity
     */
    public Review submitReview(Long questionId, Long reviewerId, String feedback, boolean approved) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found with ID: " + reviewerId));

        if (question.getStatus() == QuestionStatus.PENDING_REVIEW) {
            question.startReview();
        }

        if (approved) {
            question.approve();
        } else {
            question.reject(feedback);
        }

        questionRepository.save(question);

        Review review = new Review(questionId, reviewer, feedback, approved);
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsForQuestion(Long questionId) {
        return reviewRepository.findByQuestionId(questionId);
    }
}
