package co.edu.unicauca.bancopreguntas.review.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio pura de Revisión (sin Spring ni JPA).
 */
public class Revision {

    private Long id;
    private Long preguntaId;
    private Long revisorId;
    private String revisorNombre;
    private String revisorEmail;
    private String competencia;
    private String nivelDificultad;
    private Integer ronda;
    private EstadoRevision estado;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaDecision;

    public Revision() {
        this.ronda = 1;
        this.estado = EstadoRevision.ASIGNADA;
        this.fechaAsignacion = LocalDateTime.now();
    }

    public Revision(Long id, Long preguntaId, Long revisorId, String revisorNombre, String revisorEmail,
                    String competencia, String nivelDificultad, EstadoRevision estado,
                    LocalDateTime fechaAsignacion, LocalDateTime fechaDecision) {
        this(id, preguntaId, revisorId, revisorNombre, revisorEmail,
                competencia, nivelDificultad, 1, estado, fechaAsignacion, fechaDecision);
    }

    public Revision(Long id, Long preguntaId, Long revisorId, String revisorNombre, String revisorEmail,
                    String competencia, String nivelDificultad, Integer ronda, EstadoRevision estado,
                    LocalDateTime fechaAsignacion, LocalDateTime fechaDecision) {
        this.id = id;
        this.preguntaId = preguntaId;
        this.revisorId = revisorId;
        this.revisorNombre = revisorNombre;
        this.revisorEmail = revisorEmail;
        this.competencia = competencia;
        this.nivelDificultad = nivelDificultad;
        this.ronda = (ronda != null && ronda >= 1) ? ronda : 1;
        this.estado = estado != null ? estado : EstadoRevision.ASIGNADA;
        this.fechaAsignacion = fechaAsignacion != null ? fechaAsignacion : LocalDateTime.now();
        this.fechaDecision = fechaDecision;
    }

    public static Revision crearNueva(Long preguntaId, Long revisorId, String revisorNombre, String revisorEmail,
                                      String competencia, String nivelDificultad) {
        return crearNueva(preguntaId, revisorId, revisorNombre, revisorEmail, competencia, nivelDificultad, 1);
    }

    public static Revision crearNueva(Long preguntaId, Long revisorId, String revisorNombre, String revisorEmail,
                                      String competencia, String nivelDificultad, Integer ronda) {
        return new Revision(null, preguntaId, revisorId, revisorNombre, revisorEmail,
                competencia, nivelDificultad, ronda, EstadoRevision.ASIGNADA, LocalDateTime.now(), null);
    }

    public void reactivar() {
        this.estado = EstadoRevision.ASIGNADA;
        this.fechaAsignacion = LocalDateTime.now();
        this.fechaDecision = null;
    }

    public void evaluar() {
        if (this.estado.estaFinalizada()) {
            throw new IllegalStateException("Esta revisión ya fue finalizada previamente");
        }
        this.estado = EstadoRevision.EVALUADA;
    }

    public void aprobar() {
        if (this.estado.estaFinalizada()) {
            throw new IllegalStateException("Esta revisión ya se encuentra finalizada y no puede ser decidida nuevamente");
        }
        this.estado = EstadoRevision.APROBADA;
        this.fechaDecision = LocalDateTime.now();
    }

    public void rechazar() {
        if (this.estado.estaFinalizada()) {
            throw new IllegalStateException("Esta revisión ya se encuentra finalizada y no puede ser decidida nuevamente");
        }
        this.estado = EstadoRevision.RECHAZADA;
        this.fechaDecision = LocalDateTime.now();
    }

    public void cerrarPorRechazo() {
        if (!this.estado.estaFinalizada()) {
            this.estado = EstadoRevision.CERRADA_POR_RECHAZO;
            this.fechaDecision = LocalDateTime.now();
        }
    }

    // Getters and Setters
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
    public String getCompetencia() { return competencia; }
    public void setCompetencia(String competencia) { this.competencia = competencia; }
    public String getNivelDificultad() { return nivelDificultad; }
    public void setNivelDificultad(String nivelDificultad) { this.nivelDificultad = nivelDificultad; }
    public Integer getRonda() { return ronda != null ? ronda : 1; }
    public void setRonda(Integer ronda) { this.ronda = (ronda != null && ronda >= 1) ? ronda : 1; }
    public EstadoRevision getEstado() { return estado; }
    public void setEstado(EstadoRevision estado) { this.estado = estado; }
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
    public LocalDateTime getFechaDecision() { return fechaDecision; }
    public void setFechaDecision(LocalDateTime fechaDecision) { this.fechaDecision = fechaDecision; }
}

