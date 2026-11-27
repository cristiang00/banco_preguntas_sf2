package co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa;

import co.edu.unicauca.bancopreguntas.review.application.ports.output.RevisionRepositoryPort;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.EvaluacionRevisorEntity;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.ObservacionEntity;
import co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.RevisionEntity;
import co.edu.unicauca.bancopreguntas.review.domain.model.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.model.Evaluacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Observacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;
import co.edu.unicauca.bancopreguntas.review.repository.EvaluacionRevisorRepository;
import co.edu.unicauca.bancopreguntas.review.repository.ObservacionRepository;
import co.edu.unicauca.bancopreguntas.review.repository.RevisionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adaptador de Persistencia JPA para el puerto RevisionRepositoryPort (HU8).
 */
@Component
@Primary
public class JpaRevisionRepositoryAdapter implements RevisionRepositoryPort {

    private final RevisionRepository revisionRepository;
    private final EvaluacionRevisorRepository evaluacionRepository;
    private final ObservacionRepository observacionRepository;

    public JpaRevisionRepositoryAdapter(RevisionRepository revisionRepository,
                                       EvaluacionRevisorRepository evaluacionRepository,
                                       ObservacionRepository observacionRepository) {
        this.revisionRepository = revisionRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.observacionRepository = observacionRepository;
    }

    @Override
    public Revision save(Revision revision) {
        RevisionEntity jpa = ReviewDomainMapper.toJpa(revision);
        RevisionEntity saved = revisionRepository.save(jpa);
        return ReviewDomainMapper.toDomain(saved);
    }

    @Override
    public Optional<Revision> findById(Long id) {
        return revisionRepository.findById(id).map(ReviewDomainMapper::toDomain);
    }

    @Override
    public List<Revision> findByPreguntaId(Long preguntaId) {
        return revisionRepository.findByPreguntaId(preguntaId).stream()
                .map(ReviewDomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Revision> findByPreguntaIdAndRevisorId(Long preguntaId, Long revisorId) {
        return revisionRepository.findByPreguntaIdAndRevisorId(preguntaId, revisorId)
                .map(ReviewDomainMapper::toDomain);
    }

    @Override
    public Optional<Revision> findByPreguntaIdAndRevisorIdAndRonda(Long preguntaId, Long revisorId, Integer ronda) {
        int rTarget = (ronda != null && ronda >= 1) ? ronda : 1;
        return revisionRepository.findByPreguntaIdAndRevisorIdAndRonda(preguntaId, revisorId, rTarget)
                .map(ReviewDomainMapper::toDomain);
    }

    @Override
    public Map<String, Object> findByRevisorIdWithFilters(Long revisorId, EstadoRevision estado,
                                                          String competencia, String dificultad,
                                                          int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.EstadoRevision jpaEstado = estado != null
                ? co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity.EstadoRevision.valueOf(estado.name())
                : null;

        Page<RevisionEntity> revPage = revisionRepository.findByRevisorIdWithFilters(
                revisorId, jpaEstado, competencia, dificultad, pageable
        );

        List<Revision> domainList = revPage.getContent().stream()
                .map(ReviewDomainMapper::toDomain)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", domainList);
        result.put("currentPage", revPage.getNumber());
        result.put("totalElements", revPage.getTotalElements());
        result.put("totalPages", revPage.getTotalPages());
        return result;
    }

    @Override
    public Evaluacion saveEvaluacion(Evaluacion evaluacion) {
        EvaluacionRevisorEntity jpa = ReviewDomainMapper.toJpa(evaluacion);
        EvaluacionRevisorEntity saved = evaluacionRepository.save(jpa);
        return ReviewDomainMapper.toDomain(saved);
    }

    @Override
    public Observacion saveObservacion(Observacion observacion) {
        ObservacionEntity jpa = ReviewDomainMapper.toJpa(observacion);
        ObservacionEntity saved = observacionRepository.save(jpa);
        return ReviewDomainMapper.toDomain(saved);
    }

    @Override
    public List<Observacion> findObservacionesByRevisionId(Long revisionId) {
        return observacionRepository.findByRevisionId(revisionId).stream()
                .map(ReviewDomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Observacion> findObservacionesByPreguntaId(Long preguntaId) {
        return observacionRepository.findByPreguntaIdAndVisibleAutorTrue(preguntaId).stream()
                .map(ReviewDomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Evaluacion> findEvaluacionesByRevisionId(Long revisionId) {
        return evaluacionRepository.findByRevisionId(revisionId).stream()
                .map(ReviewDomainMapper::toDomain)
                .collect(Collectors.toList());
    }
}

