package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for TopicRecord.
 */
@Repository
public interface SpringTopicDataRepository extends JpaRepository<TopicRecord, Long> {
}
