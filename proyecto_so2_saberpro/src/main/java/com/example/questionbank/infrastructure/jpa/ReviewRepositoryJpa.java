package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Review;
import com.example.questionbank.domain.repository.ReviewRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.ReviewRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringReviewDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Concrete JPA implementation of domain ReviewRepository contract.
 */
@Repository
@Transactional
public class ReviewRepositoryJpa implements ReviewRepository {

    private final SpringReviewDataRepository springReviewDataRepository;
    private final SpringUserDataRepository springUserDataRepository;
    private final DomainMapper mapper;

    public ReviewRepositoryJpa(SpringReviewDataRepository springReviewDataRepository,
                               SpringUserDataRepository springUserDataRepository,
                               DomainMapper mapper) {
        this.springReviewDataRepository = springReviewDataRepository;
        this.springUserDataRepository = springUserDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Review save(Review review) {
        ReviewRecord record = mapper.toRecord(review);
        if (review.getReviewer() != null && review.getReviewer().getId() != null) {
            Optional<UserRecord> userOpt = springUserDataRepository.findById(review.getReviewer().getId());
            userOpt.ifPresent(record::setReviewer);
        }
        ReviewRecord saved = springReviewDataRepository.save(record);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findById(Long id) {
        return springReviewDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findByQuestionId(Long questionId) {
        return springReviewDataRepository.findByQuestionId(questionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
