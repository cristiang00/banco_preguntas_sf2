package co.edu.unicauca.bancopreguntas.review.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio pura de Evaluación (sin Spring ni JPA).
 */
public class Evaluacion {

    private Long id;
    private Long revisionId;
    private String criterio;
    private Double calificacion;
    private String comentario;
    private LocalDateTime fechaEvaluacion;

    public Evaluacion() {
        this.fechaEvaluacion = LocalDateTime.now();
    }

    public Evaluacion(Long id, Long revisionId, String criterio, Double calificacion, String comentario, LocalDateTime fechaEvaluacion) {
        this.id = id;
        this.revisionId = revisionId;
        this.criterio = criterio != null ? criterio : "General";
        this.calificacion = calificacion != null ? calificacion : 5.0;
        this.comentario = comentario;
        this.fechaEvaluacion = fechaEvaluacion != null ? fechaEvaluacion : LocalDateTime.now();
    }

    public static Evaluacion crear(Long revisionId, String criterio, Double calificacion, String comentario) {
        return new Evaluacion(null, revisionId, criterio, calificacion, comentario, LocalDateTime.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRevisionId() { return revisionId; }
    public void setRevisionId(Long revisionId) { this.revisionId = revisionId; }
    public String getCriterio() { return criterio; }
    public void setCriterio(String criterio) { this.criterio = criterio; }
    public Double getCalificacion() { return calificacion; }
    public void setCalificacion(Double calificacion) { this.calificacion = calificacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(LocalDateTime fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }
}

