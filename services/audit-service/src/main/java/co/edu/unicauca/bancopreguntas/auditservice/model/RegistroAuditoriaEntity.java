package co.edu.unicauca.bancopreguntas.auditservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_eventos", indexes = {
    @Index(name = "idx_audit_evento_id", columnList = "eventoId"),
    @Index(name = "idx_audit_aggregate_id", columnList = "aggregateId"),
    @Index(name = "idx_audit_event_type", columnList = "eventType")
})
public class RegistroAuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventoId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String aggregateId;

    private String correlationId;

    private Long usuarioId;

    private String usuarioNombre;

    @Column(nullable = false)
    private String accion;

    @Column(length = 4000)
    private String detalles;

    @Column(nullable = false)
    private LocalDateTime fechaEvento;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    public RegistroAuditoriaEntity() {
    }

    public RegistroAuditoriaEntity(String eventoId, String eventType, String aggregateId,
                                   String correlationId, Long usuarioId, String usuarioNombre,
                                   String accion, String detalles, LocalDateTime fechaEvento,
                                   LocalDateTime fechaRegistro) {
        this.eventoId = eventoId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.correlationId = correlationId;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.accion = accion;
        this.detalles = detalles;
        this.fechaEvento = fechaEvento;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }

    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
