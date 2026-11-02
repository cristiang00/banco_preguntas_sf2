package co.edu.unicauca.bancopreguntas.review.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluaciones_revisores")
public class EvaluacionRevisorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "revision_id", nullable = false)
    private Long revisionId;

    @Column(name = "criterio", nullable = false, length = 200)
    private String criterio;

    @Column(name = "calificacion", nullable = false)
    private Double calificacion;

    @Column(name = "comentario", length = 2000)
    private String comentario;

    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDateTime fechaEvaluacion;

    public EvaluacionRevisorEntity() {}

    public EvaluacionRevisorEntity(Long revisionId, String criterio, Double calificacion, String comentario) {
        this.revisionId = revisionId;
        this.criterio = criterio;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fechaEvaluacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRevisionId() { return revisionId; }
    public String getCriterio() { return criterio; }
    public Double getCalificacion() { return calificacion; }
    public String getComentario() { return comentario; }
    public LocalDateTime getFechaEvaluacion() { return fechaEvaluacion; }
}

