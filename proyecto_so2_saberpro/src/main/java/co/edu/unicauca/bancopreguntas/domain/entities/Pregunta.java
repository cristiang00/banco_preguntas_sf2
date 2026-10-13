package co.edu.unicauca.bancopreguntas.domain.entities;

import co.edu.unicauca.bancopreguntas.domain.state.DraftState;
import co.edu.unicauca.bancopreguntas.domain.state.EstadosPregunta;
import co.edu.unicauca.bancopreguntas.domain.state.QuestionState;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidad principal del Banco de Preguntas Saber PRO (contexto del patrón State).
 * <p>
 * Centraliza la jerarquía de dominio unificada: soporta múltiples tipos de preguntas
 * (selección única, selección múltiple, Verdadero/Falso), múltiples opciones de respuesta
 * y respuestas correctas.
 * Delega completamente las transiciones de su ciclo de vida en {@link QuestionState}.
 */
public class Pregunta {

    private int id;
    private String contexto;
    private String preguntaDirecta;
    private String distractor1; // Opción A por defecto
    private String distractor2; // Opción B por defecto
    private String distractor3; // Opción C por defecto
    private String distractor4; // Opción D por defecto
    private String respuestaCorrecta; // Clave(s) separadas por coma, p.ej. "A" o "A,C"
    private String justificacion;
    private String bibliografia;
    private String competencia;
    private String tema;
    private String subtema;
    private String nivelDificultad;
    private int autorId;
    private String autorNombre;
    private LocalDateTime fechaCreacion;

    // Nuevos atributos de dominio
    private TipoPregunta tipo = TipoPregunta.SELECCION_UNICA;
    private final List<OpcionRespuesta> opciones = new ArrayList<>();
    private final Set<String> respuestasCorrectas = new LinkedHashSet<>();
    private QuestionState state = new DraftState();
    private String revisor;
    private String motivoRechazo;

    public Pregunta() {
        this.opciones.addAll(tipo.opcionesPorDefecto());
    }

    /**
     * Constructor para creación con enunciado y tipo (RF1, RF2).
     */
    public Pregunta(String preguntaDirecta, TipoPregunta tipo) {
        this();
        this.preguntaDirecta = requireText(preguntaDirecta, "El enunciado de la pregunta es obligatorio");
        this.tipo = Objects.requireNonNull(tipo, "El tipo de pregunta es obligatorio");
        this.opciones.clear();
        this.opciones.addAll(tipo.opcionesPorDefecto());
    }

    public Pregunta(int id, String contexto, String preguntaDirecta, String distractor1, String distractor2,
                    String distractor3, String distractor4, String respuestaCorrecta, String justificacion,
                    String bibliografia, String competencia, String tema, String subtema, String nivelDificultad,
                    EstadoPregunta estado, int autorId, String autorNombre, LocalDateTime fechaCreacion) {
        this.id = id;
        this.contexto = contexto;
        this.preguntaDirecta = preguntaDirecta;
        this.justificacion = justificacion;
        this.bibliografia = bibliografia;
        this.competencia = competencia;
        this.tema = tema;
        this.subtema = subtema;
        this.nivelDificultad = nivelDificultad;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.fechaCreacion = fechaCreacion;
        this.state = EstadosPregunta.desde(estado);

        setDistractor1(distractor1);
        setDistractor2(distractor2);
        setDistractor3(distractor3);
        setDistractor4(distractor4);
        setRespuestaCorrecta(respuestaCorrecta);
    }

    // ------------------------------------------------------------------ Patrón State

    public void setState(QuestionState state) {
        this.state = Objects.requireNonNull(state, "El estado no puede ser nulo");
    }

    public QuestionState getState() {
        return state;
    }

    public void setQuestionState(QuestionState state) {
        setState(state);
    }

    public QuestionState getQuestionState() {
        return state;
    }

    public String getStateName() {
        return state != null ? state.getName() : "Borrador";
    }

    public EstadoPregunta getEstado() {
        return state != null ? state.getEstado() : EstadoPregunta.BORRADOR;
    }

    public void setEstado(EstadoPregunta estado) {
        this.state = EstadosPregunta.desde(estado);
    }

    /** Borrador → Pendiente de revisión. */
    public void submitForReview() {
        state.submitForReview(this);
    }

    /** Pendiente de revisión → En revisión. */
    public void assignReviewer(String reviewerName) {
        String validReviewer = requireText(reviewerName, "Debe indicar el nombre del revisor");
        state.assignReviewer(this);
        this.revisor = validReviewer;
    }

    /** En revisión → Aprobada. */
    public void approve() {
        state.approve(this);
    }

    /** En revisión → Rechazada. */
    public void reject(String reason) {
        String validReason = requireText(reason, "Debe indicar el motivo del rechazo");
        state.reject(this);
        this.motivoRechazo = validReason;
    }

    /** Rechazada → Borrador. */
    public void returnToDraft() {
        state.returnToDraft(this);
    }

    /** Aprobada → Publicada. */
    public void publish() {
        state.publish(this);
    }

    /** Publicada → Archivada. */
    public void archive() {
        state.archive(this);
    }

    /** Verifica que la pregunta pueda usarse en una prueba (solo si está Publicada). */
    public void ensureUsableInTest() {
        state.ensureUsableInTest(this);
    }

    public boolean isUsableInTest() {
        return state.isUsableInTest();
    }

    // ------------------------------------------------------------------ Opciones y Respuestas

    /**
     * Agrega una opción de respuesta (RF3).
     */
    public void addOpcion(String clave, String texto) {
        state.ensureEditable("agregar opciones de respuesta");
        if (!tipo.permiteOpcionesPersonalizadas()) {
            throw new IllegalArgumentException("Las preguntas de tipo '" + tipo.getEtiqueta()
                    + "' tienen opciones fijas; no se pueden agregar opciones");
        }
        OpcionRespuesta opcion = new OpcionRespuesta(clave, texto);
        if (buscarOpcion(opcion.clave()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una opción con la clave '" + opcion.clave() + "'");
        }
        opciones.add(opcion);
        sincronizarDistractorDesdeOpcion(opcion);
    }

    public void addOption(String key, String text) {
        addOpcion(key, text);
    }

    /**
     * Define las respuestas correctas (RF4) comprobando las reglas del tipo de pregunta.
     */
    public void definirRespuestasCorrectas(Collection<String> claves) {
        state.ensureEditable("definir las respuestas correctas");
        Objects.requireNonNull(claves, "Debe indicar las respuestas correctas");
        Set<String> normalizadas = new LinkedHashSet<>();
        for (String c : claves) {
            normalizadas.add(OpcionRespuesta.normalizarClave(requireText(c, "Las claves no pueden estar vacías")));
        }
        if (!tipo.aceptaCantidadRespuestasCorrectas(normalizadas.size())) {
            throw new IllegalArgumentException("Una pregunta de tipo '" + tipo.getEtiqueta() + "' requiere "
                    + tipo.getReglaRespuestasCorrectas() + "; se indicaron " + normalizadas.size());
        }
        for (String c : normalizadas) {
            if (buscarOpcion(c).isEmpty()) {
                throw new IllegalArgumentException("La opción '" + c + "' no existe en la pregunta");
            }
        }
        this.respuestasCorrectas.clear();
        this.respuestasCorrectas.addAll(normalizadas);
        this.respuestaCorrecta = String.join(",", normalizadas);
    }

    public void definirRespuestasCorrectas(String... claves) {
        definirRespuestasCorrectas(Arrays.asList(Objects.requireNonNull(claves, "Debe indicar las respuestas correctas")));
    }

    public void defineCorrectAnswers(String... keys) {
        definirRespuestasCorrectas(keys);
    }

    public void defineCorrectAnswers(Collection<String> keys) {
        definirRespuestasCorrectas(keys);
    }

    public Optional<OpcionRespuesta> buscarOpcion(String clave) {
        if (clave == null || clave.isBlank()) {
            return Optional.empty();
        }
        String normalizada = OpcionRespuesta.normalizarClave(clave);
        return opciones.stream().filter(o -> o.clave().equals(normalizada)).findFirst();
    }

    public Optional<OpcionRespuesta> findOption(String key) {
        return buscarOpcion(key);
    }

    public boolean isContentComplete() {
        return opciones.size() >= TipoPregunta.MIN_OPCIONES
                && tipo.aceptaCantidadRespuestasCorrectas(respuestasCorrectas.size());
    }

    public void setOpciones(List<OpcionRespuesta> nuevasOpciones) {
        state.ensureEditable("actualizar las opciones de respuesta");
        this.opciones.clear();
        if (nuevasOpciones != null) {
            for (OpcionRespuesta op : nuevasOpciones) {
                this.opciones.add(op);
                sincronizarDistractorDesdeOpcion(op);
            }
        }
    }

    public List<OpcionRespuesta> getOpciones() {
        return Collections.unmodifiableList(opciones);
    }

    public List<OpcionRespuesta> getOptions() {
        return getOpciones();
    }

    public Set<String> getRespuestasCorrectas() {
        return Collections.unmodifiableSet(respuestasCorrectas);
    }

    public Set<String> getCorrectAnswers() {
        return getRespuestasCorrectas();
    }

    public TipoPregunta getTipo() {
        return tipo;
    }

    public void setTipo(TipoPregunta tipo) {
        state.ensureEditable("modificar el tipo de pregunta");
        hidratarTipo(tipo);
        if (!this.tipo.permiteOpcionesPersonalizadas()) {
            this.opciones.clear();
            this.opciones.addAll(this.tipo.opcionesPorDefecto());
            for (OpcionRespuesta op : this.opciones) {
                sincronizarDistractorDesdeOpcion(op);
            }
        }
    }

    /** Hidratación de persistencia sin comprobación de negocio ensureEditable. */
    public void hidratarTipo(TipoPregunta tipo) {
        this.tipo = tipo != null ? tipo : TipoPregunta.SELECCION_UNICA;
    }

    /** Hidratación de opciones de persistencia sin comprobación de negocio ensureEditable. */
    public void hidratarOpciones(List<OpcionRespuesta> lista) {
        this.opciones.clear();
        if (lista != null) {
            this.opciones.addAll(lista);
            for (OpcionRespuesta op : this.opciones) {
                sincronizarDistractorDesdeOpcion(op);
            }
        }
    }

    /** Hidratación de estado de persistencia. */
    public void hidratarEstado(EstadoPregunta estado) {
        if (estado != null) {
            this.state = EstadosPregunta.desde(estado);
        }
    }

    public TipoPregunta getType() {
        return getTipo();
    }

    // ------------------------------------------------------------------ Getters / Setters tradicionales

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public void assignId(long newId) { this.id = (int) newId; }

    public String getContexto() { return contexto; }
    public void setContexto(String contexto) {
        state.ensureEditable("modificar el contexto");
        this.contexto = contexto;
    }
    public String getContext() { return getContexto(); }
    public void setContext(String context) { setContexto(context); }

    public String getPreguntaDirecta() { return preguntaDirecta; }
    public void setPreguntaDirecta(String preguntaDirecta) {
        state.ensureEditable("modificar el enunciado de la pregunta");
        this.preguntaDirecta = preguntaDirecta;
    }
    public String getStatement() { return getPreguntaDirecta(); }

    public String getDistractor1() { return distractor1; }
    public void setDistractor1(String distractor1) {
        this.distractor1 = distractor1;
        sincronizarOpcionDesdeDistractor("A", distractor1);
    }

    public String getDistractor2() { return distractor2; }
    public void setDistractor2(String distractor2) {
        this.distractor2 = distractor2;
        sincronizarOpcionDesdeDistractor("B", distractor2);
    }

    public String getDistractor3() { return distractor3; }
    public void setDistractor3(String distractor3) {
        this.distractor3 = distractor3;
        sincronizarOpcionDesdeDistractor("C", distractor3);
    }

    public String getDistractor4() { return distractor4; }
    public void setDistractor4(String distractor4) {
        this.distractor4 = distractor4;
        sincronizarOpcionDesdeDistractor("D", distractor4);
    }

    public String getRespuestaCorrecta() { return respuestaCorrecta; }
    public void setRespuestaCorrecta(String respuestaCorrecta) {
        this.respuestaCorrecta = respuestaCorrecta;
        if (respuestaCorrecta != null && !respuestaCorrecta.isBlank()) {
            this.respuestasCorrectas.clear();
            for (String parte : respuestaCorrecta.split(",")) {
                if (!parte.trim().isEmpty()) {
                    this.respuestasCorrectas.add(parte.trim().toUpperCase());
                }
            }
        }
    }

    public String getJustificacion() { return justificacion; }
    public void setJustificacion(String justificacion) {
        state.ensureEditable("modificar la justificación");
        this.justificacion = justificacion;
    }
    public String getExplanation() { return getJustificacion(); }
    public void setExplanation(String explanation) { setJustificacion(explanation); }

    public String getBibliografia() { return bibliografia; }
    public void setBibliografia(String bibliografia) {
        state.ensureEditable("modificar la bibliografía");
        this.bibliografia = bibliografia;
    }

    public String getCompetencia() { return competencia; }
    public void setCompetencia(String competencia) {
        state.ensureEditable("modificar la competencia");
        this.competencia = competencia;
    }
    public String getCompetency() { return getCompetencia(); }
    public void setCompetency(String competency) { setCompetencia(competency); }

    public String getTema() { return tema; }
    public void setTema(String tema) {
        state.ensureEditable("modificar el tema");
        this.tema = tema;
    }

    public String getSubtema() { return subtema; }
    public void setSubtema(String subtema) {
        state.ensureEditable("modificar el subtema");
        this.subtema = subtema;
    }

    public String getNivelDificultad() { return nivelDificultad; }
    public void setNivelDificultad(String nivelDificultad) {
        state.ensureEditable("modificar el nivel de dificultad");
        this.nivelDificultad = nivelDificultad;
    }

    public int getAutorId() { return autorId; }
    public void setAutorId(int autorId) { this.autorId = autorId; }

    public String getAutorNombre() { return autorNombre; }
    public void setAutorNombre(String autorNombre) { this.autorNombre = autorNombre; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getRevisor() { return revisor; }
    public void setRevisor(String revisor) { this.revisor = revisor; }
    public String getReviewer() { return getRevisor(); }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
    public String getRejectionReason() { return getMotivoRechazo(); }

    // ------------------------------------------------------------------ Sincronización interna

    private void sincronizarDistractorDesdeOpcion(OpcionRespuesta op) {
        switch (op.clave()) {
            case "A" -> this.distractor1 = op.texto();
            case "B" -> this.distractor2 = op.texto();
            case "C" -> this.distractor3 = op.texto();
            case "D" -> this.distractor4 = op.texto();
        }
    }

    private void sincronizarOpcionDesdeDistractor(String clave, String texto) {
        if (texto == null || texto.isBlank()) {
            return;
        }
        for (int i = 0; i < opciones.size(); i++) {
            if (opciones.get(i).clave().equals(clave)) {
                opciones.set(i, new OpcionRespuesta(clave, texto));
                return;
            }
        }
        opciones.add(new OpcionRespuesta(clave, texto));
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return "#" + id + " [" + tipo.getEtiqueta() + "] " + preguntaDirecta + " (estado: " + getStateName() + ")";
    }
}
