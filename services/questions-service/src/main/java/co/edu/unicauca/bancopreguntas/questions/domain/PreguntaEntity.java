package co.edu.unicauca.bancopreguntas.questions.domain;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "preguntas")
public class PreguntaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contexto", nullable = false, length = 3000)
    private String contexto;

    @Column(name = "pregunta_directa", nullable = false, length = 1500)
    private String preguntaDirecta;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pregunta_id")
    private List<OpcionEntity> opciones = new ArrayList<>();

    @Column(name = "respuesta_correcta", nullable = false, length = 50)
    private String respuestaCorrecta;

    @Column(name = "justificacion", length = 3000)
    private String justificacion;

    @Column(name = "bibliografia", nullable = false, length = 1000)
    private String bibliografia;

    @Column(name = "competencia", nullable = false, length = 150)
    private String competencia;

    @Column(name = "tema", nullable = false, length = 150)
    private String tema;

    @Column(name = "subtema", nullable = false, length = 150)
    private String subtema;

    @Column(name = "nivel_dificultad", nullable = false, length = 50)
    private String nivelDificultad;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "autor_nombre", length = 150)
    private String autorNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_proyeccion", nullable = false, length = 50)
    private EstadoPreguntaDTO estadoProyeccion = EstadoPreguntaDTO.BORRADOR;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public PreguntaEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContexto() { return contexto; }
    public void setContexto(String contexto) { this.contexto = contexto; }

    public String getPreguntaDirecta() { return preguntaDirecta; }
    public void setPreguntaDirecta(String preguntaDirecta) { this.preguntaDirecta = preguntaDirecta; }

    public List<OpcionEntity> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionEntity> opciones) { this.opciones = opciones; }

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

    public EstadoPreguntaDTO getEstadoProyeccion() { return estadoProyeccion; }
    public void setEstadoProyeccion(EstadoPreguntaDTO estadoProyeccion) { this.estadoProyeccion = estadoProyeccion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}

