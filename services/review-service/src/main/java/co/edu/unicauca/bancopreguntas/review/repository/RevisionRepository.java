package co.edu.unicauca.bancopreguntas.review.repository;

import co.edu.unicauca.bancopreguntas.review.domain.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.RevisionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RevisionRepository extends JpaRepository<RevisionEntity, Long> {

    List<RevisionEntity> findByPreguntaId(Long preguntaId);

    Optional<RevisionEntity> findByPreguntaIdAndRevisorId(Long preguntaId, Long revisorId);

    Page<RevisionEntity> findByRevisorId(Long revisorId, Pageable pageable);

    @Query("SELECT r FROM RevisionEntity r WHERE r.revisorId = :revisorId " +
           "AND (:estado IS NULL OR r.estado = :estado) " +
           "AND (:competencia IS NULL OR LOWER(r.competencia) = LOWER(:competencia)) " +
           "AND (:dificultad IS NULL OR LOWER(r.nivelDificultad) = LOWER(:dificultad))")
    Page<RevisionEntity> findByRevisorIdWithFilters(
           @Param("revisorId") Long revisorId,
           @Param("estado") EstadoRevision estado,
           @Param("competencia") String competencia,
           @Param("dificultad") String dificultad,
           Pageable pageable);

    List<RevisionEntity> findByRevisorId(Long revisorId);

    boolean existsByPreguntaIdAndRevisorId(Long preguntaId, Long revisorId);
}
