package co.edu.unicauca.bancopreguntas.questions.domain;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.OpcionDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Patrón Builder (GoF) para construir entidades {@link PreguntaEntity} de forma controlada y coherente (HU1 - SSCRUM-34).
 * Reutilizado y adaptado de {@code co.edu.unicauca.bancopreguntas.domain.entities.PreguntaBuilder}.
 */
public class PreguntaBuilder {

    private Long id;
    private String contexto;
    private String preguntaDirecta;
    private final List<OpcionEntity> opciones = new ArrayList<>();
    private String respuestaCorrecta;
    private String justificacion;
    private String bibliografia;
    private String competencia;
    private String tema;
    private String subtema;
    private String nivelDificultad;
    private Long autorId;
    private String autorNombre;
    private EstadoPreguntaDTO estado = EstadoPreguntaDTO.BORRADOR;
    private LocalDateTime fechaCreacion;

    public PreguntaBuilder conId(Long id) {
        this.id = id;
        return this;
    }

    public PreguntaBuilder conContexto(String contexto) {
        this.contexto = contexto;
        return this;
    }

    public PreguntaBuilder conPreguntaDirecta(String preguntaDirecta) {
        this.preguntaDirecta = preguntaDirecta;
        return this;
    }

    public PreguntaBuilder conOpcion(String clave, String texto) {
        if (clave != null && texto != null) {
            this.opciones.add(new OpcionEntity(clave.trim(), texto.trim(), this.opciones.size() + 1));
        }
        return this;
    }

    public PreguntaBuilder conOpciones(List<OpcionDTO> opcionesDTO) {
        if (opcionesDTO != null) {
            int orden = 1;
            for (OpcionDTO op : opcionesDTO) {
                if (op != null) {
                    this.opciones.add(new OpcionEntity(op.getClave(), op.getTexto(), orden++));
                }
            }
        }
        return this;
    }

    public PreguntaBuilder conRespuestaCorrecta(String respuestaCorrecta) {
        this.respuestaCorrecta = respuestaCorrecta != null ? respuestaCorrecta.trim().toUpperCase() : null;
        return this;
    }

    public PreguntaBuilder conJustificacion(String justificacion) {
        this.justificacion = justificacion;
        return this;
    }

    public PreguntaBuilder conBibliografia(String bibliografia) {
        this.bibliografia = bibliografia;
        return this;
    }

    public PreguntaBuilder conCompetencia(String competencia) {
        this.competencia = competencia;
        return this;
    }

    public PreguntaBuilder conTema(String tema) {
        this.tema = tema;
        return this;
    }

    public PreguntaBuilder conSubtema(String subtema) {
        this.subtema = subtema;
        return this;
    }

    public PreguntaBuilder conNivelDificultad(String nivelDificultad) {
        this.nivelDificultad = nivelDificultad;
        return this;
    }

    public PreguntaBuilder conAutorId(Long autorId) {
        this.autorId = autorId;
        return this;
    }

    public PreguntaBuilder conAutorNombre(String autorNombre) {
        this.autorNombre = autorNombre;
        return this;
    }

    public PreguntaBuilder conEstado(EstadoPreguntaDTO estado) {
        if (estado != null) {
            this.estado = estado;
        }
        return this;
    }

    public PreguntaBuilder conFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
        return this;
    }

    public PreguntaEntity build() {
        PreguntaEntity entity = new PreguntaEntity();
        entity.setId(this.id);
        entity.setContexto(this.contexto);
        entity.setPreguntaDirecta(this.preguntaDirecta);
        entity.setOpciones(new ArrayList<>(this.opciones));
        entity.setRespuestaCorrecta(this.respuestaCorrecta);
        entity.setJustificacion(this.justificacion);
        entity.setBibliografia(this.bibliografia);
        entity.setCompetencia(this.competencia);
        entity.setTema(this.tema);
        entity.setSubtema(this.subtema);
        entity.setNivelDificultad(this.nivelDificultad);
        entity.setAutorId(this.autorId);
        entity.setAutorNombre(this.autorNombre);
        entity.setEstadoProyeccion(this.estado != null ? this.estado : EstadoPreguntaDTO.BORRADOR);
        entity.setFechaCreacion(this.fechaCreacion != null ? this.fechaCreacion : LocalDateTime.now());
        return entity;
    }
}

