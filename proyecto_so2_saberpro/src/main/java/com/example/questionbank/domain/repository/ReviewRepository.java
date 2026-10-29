package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Review;

import java.util.List;
import java.util.Optional;

/**
 * Technology-agnostic domain repository contract for Review entities.
 */
public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(Long id);

    List<Review> findByQuestionId(Long questionId);
}
