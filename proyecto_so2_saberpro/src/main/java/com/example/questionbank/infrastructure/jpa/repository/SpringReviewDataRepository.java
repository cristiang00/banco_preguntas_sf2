package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ReviewRecord.
 */
@Repository
public interface SpringReviewDataRepository extends JpaRepository<ReviewRecord, Long> {

    List<ReviewRecord> findByQuestionId(Long questionId);
}
