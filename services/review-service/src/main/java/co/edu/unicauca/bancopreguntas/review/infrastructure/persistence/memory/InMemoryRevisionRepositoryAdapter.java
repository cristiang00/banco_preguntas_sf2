package co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.memory;

import co.edu.unicauca.bancopreguntas.review.application.ports.output.RevisionRepositoryPort;
import co.edu.unicauca.bancopreguntas.review.domain.model.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.model.Evaluacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Observacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Segundo adaptador de repositorio en memoria (InMemory) para el puerto RevisionRepositoryPort (HU8 - Subtarea 3).
 * Demuestra que la lógica de negocio y los casos de uso funcionan idénticamente sin tocar el dominio
 * ni requerir Spring Data JPA.
 */
@Component("inMemoryRevisionRepository")
public class InMemoryRevisionRepositoryAdapter implements RevisionRepositoryPort {

    private final Map<Long, Revision> revisiones = new ConcurrentHashMap<>();
    private final Map<Long, Evaluacion> evaluaciones = new ConcurrentHashMap<>();
    private final List<Observacion> observaciones = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong revisionSeq = new AtomicLong(1);
    private final AtomicLong evaluacionSeq = new AtomicLong(1);
    private final AtomicLong observacionSeq = new AtomicLong(1);

    public void clear() {
        revisiones.clear();
        evaluaciones.clear();
        observaciones.clear();
        revisionSeq.set(1);
        evaluacionSeq.set(1);
        observacionSeq.set(1);
    }

    @Override
    public Revision save(Revision revision) {
        if (revision == null) return null;
        if (revision.getId() == null) {
            revision.setId(revisionSeq.getAndIncrement());
        }
        revisiones.put(revision.getId(), revision);
        return revision;
    }

    @Override
    public Optional<Revision> findById(Long id) {
        return Optional.ofNullable(revisiones.get(id));
    }

    @Override
    public List<Revision> findByPreguntaId(Long preguntaId) {
        return revisiones.values().stream()
                .filter(r -> r.getPreguntaId().equals(preguntaId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Revision> findByPreguntaIdAndRevisorId(Long preguntaId, Long revisorId) {
        return revisiones.values().stream()
                .filter(r -> r.getPreguntaId().equals(preguntaId) && r.getRevisorId().equals(revisorId))
                .findFirst();
    }

    @Override
    public Optional<Revision> findByPreguntaIdAndRevisorIdAndRonda(Long preguntaId, Long revisorId, Integer ronda) {
        int rTarget = (ronda != null && ronda >= 1) ? ronda : 1;
        return revisiones.values().stream()
                .filter(r -> r.getPreguntaId().equals(preguntaId)
                        && r.getRevisorId().equals(revisorId)
                        && r.getRonda() == rTarget)
                .findFirst();
    }

    @Override
    public Map<String, Object> findByRevisorIdWithFilters(Long revisorId, EstadoRevision estado,
                                                          String competencia, String dificultad,
                                                          int page, int size) {
        List<Revision> filtered = revisiones.values().stream()
                .filter(r -> r.getRevisorId().equals(revisorId))
                .filter(r -> estado == null || r.getEstado() == estado)
                .filter(r -> competencia == null || (r.getCompetencia() != null && r.getCompetencia().equalsIgnoreCase(competencia)))
                .filter(r -> dificultad == null || (r.getNivelDificultad() != null && r.getNivelDificultad().equalsIgnoreCase(dificultad)))
                .collect(Collectors.toList());

        int pageSize = Math.max(1, size);
        int pageIndex = Math.max(0, page);
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        int start = pageIndex * pageSize;
        int end = Math.min(start + pageSize, totalElements);
        List<Revision> content = (start < totalElements) ? filtered.subList(start, end) : Collections.emptyList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("currentPage", pageIndex);
        result.put("totalElements", (long) totalElements);
        result.put("totalPages", totalPages);
        return result;
    }

    @Override
    public Evaluacion saveEvaluacion(Evaluacion evaluacion) {
        if (evaluacion == null) return null;
        if (evaluacion.getId() == null) {
            evaluacion.setId(evaluacionSeq.getAndIncrement());
        }
        evaluaciones.put(evaluacion.getId(), evaluacion);
        return evaluacion;
    }

    @Override
    public Observacion saveObservacion(Observacion observacion) {
        if (observacion == null) return null;
        if (observacion.getId() == null) {
            observacion.setId(observacionSeq.getAndIncrement());
        }
        observaciones.add(observacion);
        return observacion;
    }

    @Override
    public List<Observacion> findObservacionesByRevisionId(Long revisionId) {
        return observaciones.stream()
                .filter(o -> o.getRevisionId().equals(revisionId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Observacion> findObservacionesByPreguntaId(Long preguntaId) {
        return observaciones.stream()
                .filter(o -> o.getPreguntaId().equals(preguntaId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Evaluacion> findEvaluacionesByRevisionId(Long revisionId) {
        return evaluaciones.values().stream()
                .filter(e -> e.getRevisionId().equals(revisionId))
                .collect(Collectors.toList());
    }
}

