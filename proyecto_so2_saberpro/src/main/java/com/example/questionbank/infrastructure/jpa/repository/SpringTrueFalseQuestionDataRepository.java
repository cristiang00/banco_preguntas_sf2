package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.TrueFalseQuestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for specialized TrueFalseQuestionRecord.
 */
@Repository
public interface SpringTrueFalseQuestionDataRepository extends JpaRepository<TrueFalseQuestionRecord, Long> {
}
