package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for QuizRecord.
 */
@Repository
public interface SpringQuizDataRepository extends JpaRepository<QuizRecord, Long> {
}
