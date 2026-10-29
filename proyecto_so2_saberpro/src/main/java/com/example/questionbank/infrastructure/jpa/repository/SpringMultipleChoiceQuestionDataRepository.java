package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.MultipleChoiceQuestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for specialized MultipleChoiceQuestionRecord.
 */
@Repository
public interface SpringMultipleChoiceQuestionDataRepository extends JpaRepository<MultipleChoiceQuestionRecord, Long> {
}
