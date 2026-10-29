package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for CompetencyRecord.
 */
@Repository
public interface SpringCompetencyDataRepository extends JpaRepository<CompetencyRecord, Long> {

    Optional<CompetencyRecord> findByCode(String code);
}
