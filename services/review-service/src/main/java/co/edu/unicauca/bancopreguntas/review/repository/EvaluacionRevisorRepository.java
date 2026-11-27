package co.edu.unicauca.bancopreguntas.review.repository;

import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.EvaluacionRevisorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluacionRevisorRepository extends JpaRepository<EvaluacionRevisorEntity, Long> {
    List<EvaluacionRevisorEntity> findByRevisionId(Long revisionId);
}

