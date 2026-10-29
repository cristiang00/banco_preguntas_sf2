package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository interface for QuestionRecord.
 */
@Repository
public interface SpringQuestionDataRepository extends JpaRepository<QuestionRecord, Long> {

    List<QuestionRecord> findByAuthorId(Long authorId);

    List<QuestionRecord> findByStatus(QuestionStatus status);

    @Query("SELECT q FROM QuestionRecord q JOIN q.competencies c WHERE c.id = :competencyId AND q.status = :status")
    List<QuestionRecord> findByCompetencyIdAndStatus(@Param("competencyId") Long competencyId, @Param("status") QuestionStatus status);
}
