package co.edu.unicauca.bancopreguntas.review.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "revisiones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"pregunta_id", "revisor_id"})
})
public class RevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pregunta_id", nullable = false)
    private Long preguntaId;

    @Column(name = "revisor_id", nullable = false)
    private Long revisorId;

    @Column(name = "revisor_nombre", length = 150)
    private String revisorNombre;

    @Column(name = "revisor_email", length = 150)
    private String revisorEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoRevision estado = EstadoRevision.ASIGNADA;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_decision")
    private LocalDateTime fechaDecision;

    public RevisionEntity() {}

    public RevisionEntity(Long preguntaId, Long revisorId, String revisorNombre, String revisorEmail) {
        this.preguntaId = preguntaId;
        this.revisorId = revisorId;
        this.revisorNombre = revisorNombre;
        this.revisorEmail = revisorEmail;
        this.estado = EstadoRevision.ASIGNADA;
        this.fechaAsignacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPreguntaId() { return preguntaId; }
    public void setPreguntaId(Long preguntaId) { this.preguntaId = preguntaId; }

    public Long getRevisorId() { return revisorId; }
    public void setRevisorId(Long revisorId) { this.revisorId = revisorId; }

    public String getRevisorNombre() { return revisorNombre; }
    public void setRevisorNombre(String revisorNombre) { this.revisorNombre = revisorNombre; }

    public String getRevisorEmail() { return revisorEmail; }
    public void setRevisorEmail(String revisorEmail) { this.revisorEmail = revisorEmail; }

    public EstadoRevision getEstado() { return estado; }
    public void setEstado(EstadoRevision estado) { this.estado = estado; }

    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public LocalDateTime getFechaDecision() { return fechaDecision; }
    public void setFechaDecision(LocalDateTime fechaDecision) { this.fechaDecision = fechaDecision; }
}
