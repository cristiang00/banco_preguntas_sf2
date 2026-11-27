package co.edu.unicauca.bancopreguntas.questions.domain;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Snapshot inmutable de una versión anterior de una pregunta (HU6.S3).
 * Garantiza que al crear una versión mejorada se conserven las versiones previas y su historial.
 */
@Entity
@Table(name = "pregunta_versiones")
public class PreguntaVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pregunta_id", nullable = false)
    private Long preguntaId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "contexto", nullable = false, length = 3000)
    private String contexto;

    @Column(name = "pregunta_directa", nullable = false, length = 1500)
    private String preguntaDirecta;

    @Column(name = "opciones_resumen", length = 4000)
    private String opcionesResumen;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_al_archivar", nullable = false, length = 50)
    private EstadoPreguntaDTO estadoAlArchivar;

    @Column(name = "fecha_archivado", nullable = false)
    private LocalDateTime fechaArchivado;

    public PreguntaVersionEntity() {}

    public static PreguntaVersionEntity fromPregunta(PreguntaEntity entity) {
        PreguntaVersionEntity snapshot = new PreguntaVersionEntity();
        snapshot.preguntaId = entity.getId();
        snapshot.version = entity.getVersion();
        snapshot.contexto = entity.getContexto();
        snapshot.preguntaDirecta = entity.getPreguntaDirecta();
        if (entity.getOpciones() != null) {
            snapshot.opcionesResumen = entity.getOpciones().stream()
                    .map(op -> op.getClave() + ") " + op.getTexto())
                    .collect(Collectors.joining(" | "));
        }
        snapshot.respuestaCorrecta = entity.getRespuestaCorrecta();
        snapshot.justificacion = entity.getJustificacion();
        snapshot.bibliografia = entity.getBibliografia();
        snapshot.competencia = entity.getCompetencia();
        snapshot.tema = entity.getTema();
        snapshot.subtema = entity.getSubtema();
        snapshot.nivelDificultad = entity.getNivelDificultad();
        snapshot.autorId = entity.getAutorId();
        snapshot.estadoAlArchivar = entity.getEstadoProyeccion() != null
                ? entity.getEstadoProyeccion()
                : EstadoPreguntaDTO.RECHAZADA;
        snapshot.fechaArchivado = LocalDateTime.now();
        return snapshot;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPreguntaId() { return preguntaId; }
    public Integer getVersion() { return version; }
    public String getContexto() { return contexto; }
    public String getPreguntaDirecta() { return preguntaDirecta; }
    public String getOpcionesResumen() { return opcionesResumen; }
    public String getRespuestaCorrecta() { return respuestaCorrecta; }
    public String getJustificacion() { return justificacion; }
    public String getBibliografia() { return bibliografia; }
    public String getCompetencia() { return competencia; }
    public String getTema() { return tema; }
    public String getSubtema() { return subtema; }
    public String getNivelDificultad() { return nivelDificultad; }
    public Long getAutorId() { return autorId; }
    public EstadoPreguntaDTO getEstadoAlArchivar() { return estadoAlArchivar; }
    public LocalDateTime getFechaArchivado() { return fechaArchivado; }
}

