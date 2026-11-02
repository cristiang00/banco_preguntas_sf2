package co.edu.unicauca.bancopreguntas.frontend.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RevisionItemDTO {
    private Long id;
    private Long preguntaId;
    private String tituloPregunta;
    private String contexto;
    private String estadoRevision;
    private String resultadoConsenso;
    private String fechaAsignacion;

    public RevisionItemDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPreguntaId() { return preguntaId; }
    public void setPreguntaId(Long preguntaId) { this.preguntaId = preguntaId; }

    public String getTituloPregunta() { return tituloPregunta; }
    public void setTituloPregunta(String tituloPregunta) { this.tituloPregunta = tituloPregunta; }

    public String getContexto() { return contexto; }
    public void setContexto(String contexto) { this.contexto = contexto; }

    public String getEstadoRevision() { return estadoRevision; }
    public void setEstadoRevision(String estadoRevision) { this.estadoRevision = estadoRevision; }

    public String getResultadoConsenso() { return resultadoConsenso; }
    public void setResultadoConsenso(String resultadoConsenso) { this.resultadoConsenso = resultadoConsenso; }

    public String getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(String fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
}
