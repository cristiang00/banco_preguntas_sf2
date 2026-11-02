package co.edu.unicauca.bancopreguntas.review.repository;

import co.edu.unicauca.bancopreguntas.review.domain.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.RevisionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RevisionRepository extends JpaRepository<RevisionEntity, Long> {

    List<RevisionEntity> findByPreguntaId(Long preguntaId);

    Optional<RevisionEntity> findByPreguntaIdAndRevisorId(Long preguntaId, Long revisorId);

    Page<RevisionEntity> findByRevisorId(Long revisorId, Pageable pageable);

    List<RevisionEntity> findByRevisorId(Long revisorId);

    boolean existsByPreguntaIdAndRevisorId(Long preguntaId, Long revisorId);
}

