package co.edu.unicauca.bancopreguntas.review.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio pura de Observación (sin Spring ni JPA).
 */
public class Observacion {

    private Long id;
    private Long revisionId;
    private Long preguntaId;
    private String texto;
    private LocalDateTime fechaCreacion;
    private boolean visibleAutor;

    public Observacion() {
        this.fechaCreacion = LocalDateTime.now();
        this.visibleAutor = true;
    }

    public Observacion(Long id, Long revisionId, Long preguntaId, String texto, LocalDateTime fechaCreacion, boolean visibleAutor) {
        this.id = id;
        this.revisionId = revisionId;
        this.preguntaId = preguntaId;
        this.texto = texto;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.visibleAutor = visibleAutor;
    }

    public static Observacion crear(Long revisionId, Long preguntaId, String texto) {
        return new Observacion(null, revisionId, preguntaId, texto, LocalDateTime.now(), true);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRevisionId() { return revisionId; }
    public void setRevisionId(Long revisionId) { this.revisionId = revisionId; }
    public Long getPreguntaId() { return preguntaId; }
    public void setPreguntaId(Long preguntaId) { this.preguntaId = preguntaId; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public boolean isVisibleAutor() { return visibleAutor; }
    public void setVisibleAutor(boolean visibleAutor) { this.visibleAutor = visibleAutor; }
}

