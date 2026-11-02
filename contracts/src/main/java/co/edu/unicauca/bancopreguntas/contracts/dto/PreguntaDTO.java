package co.edu.unicauca.bancopreguntas.contracts.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PreguntaDTO implements Serializable {

    private Long id;
    private String contexto;
    private String preguntaDirecta;
    private List<OpcionDTO> opciones = new ArrayList<>();
    private String respuestaCorrecta;
    private String justificacion;
    private String bibliografia;
    private String competencia;
    private String tema;
    private String subtema;
    private String nivelDificultad;
    private Long autorId;
    private String autorNombre;
    private String estado;
    private String estadoColor;
    private LocalDateTime fechaCreacion;
    private List<String> accionesPermitidas = new ArrayList<>();
    private List<String> observaciones = new ArrayList<>();

    public PreguntaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContexto() { return contexto; }
    public void setContexto(String contexto) { this.contexto = contexto; }

    public String getPreguntaDirecta() { return preguntaDirecta; }
    public void setPreguntaDirecta(String preguntaDirecta) { this.preguntaDirecta = preguntaDirecta; }

    public List<OpcionDTO> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionDTO> opciones) { this.opciones = opciones; }

    public String getRespuestaCorrecta() { return respuestaCorrecta; }
    public void setRespuestaCorrecta(String respuestaCorrecta) { this.respuestaCorrecta = respuestaCorrecta; }

    public String getJustificacion() { return justificacion; }
    public void setJustificacion(String justificacion) { this.justificacion = justificacion; }

    public String getBibliografia() { return bibliografia; }
    public void setBibliografia(String bibliografia) { this.bibliografia = bibliografia; }

    public String getCompetencia() { return competencia; }
    public void setCompetencia(String competencia) { this.competencia = competencia; }

    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }

    public String getSubtema() { return subtema; }
    public void setSubtema(String subtema) { this.subtema = subtema; }

    public String getNivelDificultad() { return nivelDificultad; }
    public void setNivelDificultad(String nivelDificultad) { this.nivelDificultad = nivelDificultad; }

    public Long getAutorId() { return autorId; }
    public void setAutorId(Long autorId) { this.autorId = autorId; }

    public String getAutorNombre() { return autorNombre; }
    public void setAutorNombre(String autorNombre) { this.autorNombre = autorNombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEstadoColor() { return estadoColor; }
    public void setEstadoColor(String estadoColor) { this.estadoColor = estadoColor; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public List<String> getAccionesPermitidas() { return accionesPermitidas; }
    public void setAccionesPermitidas(List<String> accionesPermitidas) { this.accionesPermitidas = accionesPermitidas; }

    public List<String> getObservaciones() { return observaciones; }
    public void setObservaciones(List<String> observaciones) { this.observaciones = observaciones; }
}

