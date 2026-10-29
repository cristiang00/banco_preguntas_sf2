package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for UserRecord.
 */
@Repository
public interface SpringUserDataRepository extends JpaRepository<UserRecord, Long> {

    Optional<UserRecord> findByUsername(String username);
}
