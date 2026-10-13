package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.TipoPregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PreguntaService {
    private final PreguntaRepository preguntaRepository;

    /** Longitud mínima exigida a cada distractor. */
    private static final int MIN_LONGITUD_DISTRACTOR = 10;

    public PreguntaService(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }

    public void crearPregunta(Pregunta pregunta, int autorId) {
        validarCampos(pregunta);
        pregunta.setEstado(EstadoPregunta.BORRADOR);
        pregunta.setAutorId(autorId);
        pregunta.setFechaCreacion(LocalDateTime.now());
        preguntaRepository.guardar(pregunta);
    }

    public void save(Pregunta pregunta) {
        if (pregunta.getId() <= 0) {
            pregunta.setEstado(EstadoPregunta.BORRADOR);
            if (pregunta.getFechaCreacion() == null) {
                pregunta.setFechaCreacion(LocalDateTime.now());
            }
            preguntaRepository.guardar(pregunta);
        } else {
            preguntaRepository.actualizar(pregunta);
        }
    }

    public void approve(Pregunta pregunta) {
        pregunta.approve();
        if (pregunta.getId() > 0) {
            preguntaRepository.actualizarEstado(pregunta.getId(), pregunta.getEstado());
        }
    }

    public void editarPregunta(Pregunta pregunta, int autorId) {
        Optional<Pregunta> opt = preguntaRepository.buscarPorId(pregunta.getId());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("La pregunta no existe");
        }
        Pregunta existente = opt.get();
        if (existente.getAutorId() != autorId) {
            throw new IllegalArgumentException("No tiene permisos para modificar esta pregunta");
        }
        try {
            existente.getState().ensureEditable("modificar esta pregunta");
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("La pregunta no puede modificarse mientras esté en revisión", e);
        }
        validarCampos(pregunta);
        pregunta.setEstado(EstadoPregunta.BORRADOR); // mantiene estado
        pregunta.setAutorId(autorId);
        preguntaRepository.actualizar(pregunta);
    }

    public void enviarARevision(int preguntaId, int autorId) {
        new EnviarARevisionTransicion(preguntaRepository).ejecutar(preguntaId, autorId);
    }

    public void aprobarPregunta(int preguntaId) {
        Pregunta p = obtenerPreguntaOError(preguntaId);
        p.approve();
        preguntaRepository.actualizarEstado(preguntaId, p.getEstado());
    }

    public void rechazarPregunta(int preguntaId, String motivo) {
        Pregunta p = obtenerPreguntaOError(preguntaId);
        p.reject(motivo);
        preguntaRepository.actualizarEstado(preguntaId, p.getEstado());
    }

    public void publicarPregunta(int preguntaId) {
        Pregunta p = obtenerPreguntaOError(preguntaId);
        p.publish();
        preguntaRepository.actualizarEstado(preguntaId, p.getEstado());
    }

    public void archivarPregunta(int preguntaId) {
        Pregunta p = obtenerPreguntaOError(preguntaId);
        p.archive();
        preguntaRepository.actualizarEstado(preguntaId, p.getEstado());
    }

    public void devolverABorrador(int preguntaId) {
        Pregunta p = obtenerPreguntaOError(preguntaId);
        p.returnToDraft();
        preguntaRepository.actualizarEstado(preguntaId, p.getEstado());
    }

    public List<Pregunta> listarMisPreguntas(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad,
                                            int offset, int limit) {
        return preguntaRepository.listarPorAutor(autorId, estadosFiltro, nivelDificultad, offset, limit);
    }

    public int contarMisPreguntas(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        return preguntaRepository.contarPorAutor(autorId, estadosFiltro, nivelDificultad);
    }

    public List<Pregunta> listarPorRevisor(int revisorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad,
                                          int offset, int limit) {
        return preguntaRepository.listarPorRevisor(revisorId, estadosFiltro, nivelDificultad, offset, limit);
    }

    public int contarPorRevisor(int revisorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        return preguntaRepository.contarPorRevisor(revisorId, estadosFiltro, nivelDificultad);
    }

    public Pregunta obtenerPregunta(int preguntaId) {
        return preguntaRepository.buscarPorId(preguntaId).orElse(null);
    }

    private Pregunta obtenerPreguntaOError(int preguntaId) {
        return preguntaRepository.buscarPorId(preguntaId)
                .orElseThrow(() -> new IllegalArgumentException("La pregunta con ID " + preguntaId + " no existe"));
    }

    // Métodos de validación privados

    public void validarCampos(Pregunta pregunta) {
        if (pregunta.getContexto() == null || pregunta.getContexto().trim().isEmpty() ||
                pregunta.getPreguntaDirecta() == null || pregunta.getPreguntaDirecta().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe completar el contexto y la pregunta directa");
        }

        // HU01: justificación obligatoria
        if (pregunta.getJustificacion() == null || pregunta.getJustificacion().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe registrar la justificación de la respuesta");
        }

        if (pregunta.getNivelDificultad() == null || pregunta.getNivelDificultad().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar el nivel de dificultad");
        }

        if (pregunta.getBibliografia() == null || pregunta.getBibliografia().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe registrar la bibliografía de referencia");
        }

        if (pregunta.getCompetencia() == null || pregunta.getCompetencia().trim().isEmpty() ||
                pregunta.getTema() == null || pregunta.getTema().trim().isEmpty() ||
                pregunta.getSubtema() == null || pregunta.getSubtema().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar competencia, tema y subtema");
        }

        // Validación según tipo de pregunta
        if (pregunta.getTipo() == TipoPregunta.VERDADERO_FALSO) {
            if (pregunta.getRespuestaCorrecta() == null || pregunta.getRespuestaCorrecta().trim().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar una respuesta correcta");
            }
        } else {
            validarDistractores(pregunta);
            if (pregunta.getRespuestaCorrecta() == null || pregunta.getRespuestaCorrecta().trim().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar una respuesta correcta");
            }
        }

        validarExpresionesProhibidas(pregunta.getPreguntaDirecta());
    }

    private void validarDistractores(Pregunta pregunta) {
        if (pregunta.getDistractor1() == null || pregunta.getDistractor1().trim().isEmpty() ||
                pregunta.getDistractor2() == null || pregunta.getDistractor2().trim().isEmpty() ||
                pregunta.getDistractor3() == null || pregunta.getDistractor3().trim().isEmpty() ||
                pregunta.getDistractor4() == null || pregunta.getDistractor4().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe registrar los cuatro distractores");
        }

        validarExpresionesProhibidas(pregunta.getDistractor1());
        validarExpresionesProhibidas(pregunta.getDistractor2());
        validarExpresionesProhibidas(pregunta.getDistractor3());
        validarExpresionesProhibidas(pregunta.getDistractor4());

        validarEstructuraDistractor(pregunta.getDistractor1());
        validarEstructuraDistractor(pregunta.getDistractor2());
        validarEstructuraDistractor(pregunta.getDistractor3());
        validarEstructuraDistractor(pregunta.getDistractor4());

        validarDistractoresSinDuplicados(pregunta);
    }

    private void validarExpresionesProhibidas(String texto) {
        if (texto == null)
            return;
        String lower = texto.toLowerCase();
        lower = lower.replaceAll("[áàäâã]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöôõ]", "o")
                .replaceAll("[úùüû]", "u");

        if (lower.contains("todas las anteriores") || lower.contains("ninguna de las anteriores")) {
            throw new IllegalArgumentException(
                    "No se permiten expresiones como 'Todas las anteriores' o 'Ninguna de las anteriores'");
        }
    }

    private void validarEstructuraDistractor(String distractor) {
        if (distractor == null)
            return;
        String trimmed = distractor.trim();
        if (trimmed.length() < MIN_LONGITUD_DISTRACTOR) {
            throw new IllegalArgumentException("Los distractores deben cumplir con una longitud y estructura mínimas");
        }
        boolean tieneWordSignificativa = Arrays.stream(trimmed.split("\\s+"))
                .anyMatch(w -> w.length() > 2);
        if (!tieneWordSignificativa) {
            throw new IllegalArgumentException("Los distractores deben cumplir con una longitud y estructura mínimas");
        }
    }

    private void validarDistractoresSinDuplicados(Pregunta pregunta) {
        List<String> distractores = Arrays.asList(
                pregunta.getDistractor1().trim(),
                pregunta.getDistractor2().trim(),
                pregunta.getDistractor3().trim(),
                pregunta.getDistractor4().trim());
        long distintos = distractores.stream().map(String::toLowerCase).distinct().count();
        if (distintos < distractores.size()) {
            throw new IllegalArgumentException("Los distractores no pueden ser idénticos entre sí");
        }
        String pd = pregunta.getPreguntaDirecta().trim().toLowerCase();
        for (String d : distractores) {
            if (d.toLowerCase().equals(pd)) {
                throw new IllegalArgumentException(
                        "Un distractor no puede ser igual al enunciado de la pregunta directa");
            }
        }
    }
}
