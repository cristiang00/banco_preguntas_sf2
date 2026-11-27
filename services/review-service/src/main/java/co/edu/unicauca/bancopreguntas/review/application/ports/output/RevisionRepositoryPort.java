package co.edu.unicauca.bancopreguntas.review.application.ports.output;

import co.edu.unicauca.bancopreguntas.review.domain.model.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.model.Evaluacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Observacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia del agregado de Revisión (Hexagonal Architecture).
 */
public interface RevisionRepositoryPort {

    Revision save(Revision revision);

    Optional<Revision> findById(Long id);

    List<Revision> findByPreguntaId(Long preguntaId);

    Optional<Revision> findByPreguntaIdAndRevisorId(Long preguntaId, Long revisorId);

    default Optional<Revision> findByPreguntaIdAndRevisorIdAndRonda(Long preguntaId, Long revisorId, Integer ronda) {
        int rTarget = (ronda != null && ronda >= 1) ? ronda : 1;
        return findByPreguntaId(preguntaId).stream()
                .filter(r -> r.getRevisorId().equals(revisorId) && r.getRonda() == rTarget)
                .findFirst();
    }

    Map<String, Object> findByRevisorIdWithFilters(Long revisorId, EstadoRevision estado,
                                                   String competencia, String dificultad,
                                                   int page, int size);

    Evaluacion saveEvaluacion(Evaluacion evaluacion);

    Observacion saveObservacion(Observacion observacion);

    List<Observacion> findObservacionesByRevisionId(Long revisionId);

    List<Observacion> findObservacionesByPreguntaId(Long preguntaId);

    List<Evaluacion> findEvaluacionesByRevisionId(Long revisionId);
}

