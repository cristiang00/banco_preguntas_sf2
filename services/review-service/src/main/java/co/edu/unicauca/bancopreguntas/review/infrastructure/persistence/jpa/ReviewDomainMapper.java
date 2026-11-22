package co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa;

import co.edu.unicauca.bancopreguntas.review.domain.EvaluacionRevisorEntity;
import co.edu.unicauca.bancopreguntas.review.domain.ObservacionEntity;
import co.edu.unicauca.bancopreguntas.review.domain.RevisionEntity;
import co.edu.unicauca.bancopreguntas.review.domain.model.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.model.Evaluacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Observacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;

/**
 * Mapper bidireccional entre Entidades JPA y Modelos de Dominio Puro (Hexagonal Architecture).
 */
public final class ReviewDomainMapper {

    private ReviewDomainMapper() {}

    public static Revision toDomain(RevisionEntity jpa) {
        if (jpa == null) return null;
        EstadoRevision estado = jpa.getEstado() != null
                ? EstadoRevision.valueOf(jpa.getEstado().name())
                : EstadoRevision.ASIGNADA;

        return new Revision(
                jpa.getId(),
                jpa.getPreguntaId(),
                jpa.getRevisorId(),
                jpa.getRevisorNombre(),
                jpa.getRevisorEmail(),
                jpa.getCompetencia(),
                jpa.getNivelDificultad(),
                estado,
                jpa.getFechaAsignacion(),
                jpa.getFechaDecision()
        );
    }

    public static RevisionEntity toJpa(Revision domain) {
        if (domain == null) return null;
        co.edu.unicauca.bancopreguntas.review.domain.EstadoRevision estado = domain.getEstado() != null
                ? co.edu.unicauca.bancopreguntas.review.domain.EstadoRevision.valueOf(domain.getEstado().name())
                : co.edu.unicauca.bancopreguntas.review.domain.EstadoRevision.ASIGNADA;

        RevisionEntity entity = new RevisionEntity(
                domain.getPreguntaId(),
                domain.getRevisorId(),
                domain.getRevisorNombre(),
                domain.getRevisorEmail(),
                domain.getCompetencia(),
                domain.getNivelDificultad()
        );
        entity.setId(domain.getId());
        entity.setEstado(estado);
        entity.setFechaAsignacion(domain.getFechaAsignacion());
        entity.setFechaDecision(domain.getFechaDecision());
        return entity;
    }

    public static Evaluacion toDomain(EvaluacionRevisorEntity jpa) {
        if (jpa == null) return null;
        return new Evaluacion(
                jpa.getId(),
                jpa.getRevisionId(),
                jpa.getCriterio(),
                jpa.getCalificacion(),
                jpa.getComentario(),
                jpa.getFechaEvaluacion()
        );
    }

    public static EvaluacionRevisorEntity toJpa(Evaluacion domain) {
        if (domain == null) return null;
        EvaluacionRevisorEntity entity = new EvaluacionRevisorEntity(
                domain.getRevisionId(),
                domain.getCriterio(),
                domain.getCalificacion(),
                domain.getComentario()
        );
        entity.setId(domain.getId());
        return entity;
    }

    public static Observacion toDomain(ObservacionEntity jpa) {
        if (jpa == null) return null;
        return new Observacion(
                jpa.getId(),
                jpa.getRevisionId(),
                jpa.getPreguntaId(),
                jpa.getTexto(),
                jpa.getFecha(),
                jpa.isVisibleAutor()
        );
    }

    public static ObservacionEntity toJpa(Observacion domain) {
        if (domain == null) return null;
        ObservacionEntity entity = new ObservacionEntity(
                domain.getRevisionId(),
                domain.getPreguntaId(),
                domain.getTexto()
        );
        entity.setId(domain.getId());
        entity.setVisibleAutor(domain.isVisibleAutor());
        return entity;
    }
}
