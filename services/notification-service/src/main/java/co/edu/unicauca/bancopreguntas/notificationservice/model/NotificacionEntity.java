package co.edu.unicauca.bancopreguntas.notificationservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones", indexes = {
    @Index(name = "idx_notif_evento_dest", columnList = "eventoId, destinatario"),
    @Index(name = "idx_notif_destinatario", columnList = "destinatario")
})
public class NotificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventoId;

    private String correlacionId;

    @Column(nullable = false)
    private String destinatario;

    @Column(nullable = false)
    private String asunto;

    @Column(length = 4000, nullable = false)
    private String cuerpo;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String estado; // ENVIADA, FALLIDA, DUPLICADA_IGNORADA

    private int intentos;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaEnvio;

    @Column(length = 1000)
    private String errorDetalle;

    public NotificacionEntity() {
    }

    public NotificacionEntity(String eventoId, String correlacionId, String destinatario,
                              String asunto, String cuerpo, String tipo, String estado,
                              int intentos, LocalDateTime fechaCreacion, LocalDateTime fechaEnvio,
                              String errorDetalle) {
        this.eventoId = eventoId;
        this.correlacionId = correlacionId;
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.cuerpo = cuerpo;
        this.tipo = tipo;
        this.estado = estado;
        this.intentos = intentos;
        this.fechaCreacion = fechaCreacion;
        this.fechaEnvio = fechaEnvio;
        this.errorDetalle = errorDetalle;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }

    public String getCorrelacionId() { return correlacionId; }
    public void setCorrelacionId(String correlacionId) { this.correlacionId = correlacionId; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getErrorDetalle() { return errorDetalle; }
    public void setErrorDetalle(String errorDetalle) { this.errorDetalle = errorDetalle; }
}

