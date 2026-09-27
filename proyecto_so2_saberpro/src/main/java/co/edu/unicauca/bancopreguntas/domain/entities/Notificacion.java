package co.edu.unicauca.bancopreguntas.domain.entities;

import java.time.LocalDateTime;

/**
 * Entidad que representa una notificación generada al asignar un revisor a una pregunta.
 * Se persiste en SQLite a través de {@code NotificacionRepositorySQLite}.
 */
public class Notificacion {
    private int id;
    private int preguntaId;
    private int revisorId;
    private String asunto;
    private String cuerpo;
    private LocalDateTime fecha;

    public Notificacion() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPreguntaId() { return preguntaId; }
    public void setPreguntaId(int preguntaId) { this.preguntaId = preguntaId; }

    public int getRevisorId() { return revisorId; }
    public void setRevisorId(int revisorId) { this.revisorId = revisorId; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
