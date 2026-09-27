package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
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

    public void editarPregunta(Pregunta pregunta, int autorId) {
        Optional<Pregunta> opt = preguntaRepository.buscarPorId(pregunta.getId());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("La pregunta no existe");
        }
        Pregunta existente = opt.get();
        if (existente.getAutorId() != autorId) {
            throw new IllegalArgumentException("No tiene permisos para modificar esta pregunta");
        }
        if (existente.getEstado() != EstadoPregunta.BORRADOR) {
            throw new IllegalArgumentException("La pregunta no puede modificarse mientras esté en revisión");
        }
        validarCampos(pregunta);
        pregunta.setEstado(EstadoPregunta.BORRADOR); // mantiene estado
        pregunta.setAutorId(autorId);
        preguntaRepository.actualizar(pregunta);
    }

    public void enviarARevision(int preguntaId, int autorId) {
        new EnviarARevisionTransicion(preguntaRepository).ejecutar(preguntaId, autorId);
    }

    public List<Pregunta> listarMisPreguntas(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad,
            int offset, int limit) {
        return preguntaRepository.listarPorAutor(autorId, estadosFiltro, nivelDificultad, offset, limit);
    }

    public int contarMisPreguntas(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        return preguntaRepository.contarPorAutor(autorId, estadosFiltro, nivelDificultad);
    }

    public Pregunta obtenerPregunta(int preguntaId) {
        return preguntaRepository.buscarPorId(preguntaId).orElse(null);
    }

    // Métodos de validación privados

    private void validarCampos(Pregunta pregunta) {
        if (pregunta.getContexto() == null || pregunta.getContexto().trim().isEmpty() ||
                pregunta.getPreguntaDirecta() == null || pregunta.getPreguntaDirecta().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe completar el contexto y la pregunta directa");
        }

        if (pregunta.getDistractor1() == null || pregunta.getDistractor1().trim().isEmpty() ||
                pregunta.getDistractor2() == null || pregunta.getDistractor2().trim().isEmpty() ||
                pregunta.getDistractor3() == null || pregunta.getDistractor3().trim().isEmpty() ||
                pregunta.getDistractor4() == null || pregunta.getDistractor4().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe registrar los cuatro distractores");
        }

        if (pregunta.getRespuestaCorrecta() == null || pregunta.getRespuestaCorrecta().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar una respuesta correcta");
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

        // Validación de expresiones prohibidas
        validarExpresionesProhibidas(pregunta.getPreguntaDirecta());
        validarExpresionesProhibidas(pregunta.getDistractor1());
        validarExpresionesProhibidas(pregunta.getDistractor2());
        validarExpresionesProhibidas(pregunta.getDistractor3());
        validarExpresionesProhibidas(pregunta.getDistractor4());

        // Validación de longitud/estructura mínima de distractores
        validarEstructuraDistractor(pregunta.getDistractor1());
        validarEstructuraDistractor(pregunta.getDistractor2());
        validarEstructuraDistractor(pregunta.getDistractor3());
        validarEstructuraDistractor(pregunta.getDistractor4());

        // distractores no pueden ser idénticos entre sí ni iguales a la
        // pregunta directa
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

    /**
     * Verifica que el distractor tenga longitud mínima razonable y
     * al menos una palabra con más de 2 letras (evita strings triviales como "xx
     * xx").
     */
    private void validarEstructuraDistractor(String distractor) {
        if (distractor == null)
            return;
        String trimmed = distractor.trim();
        if (trimmed.length() < MIN_LONGITUD_DISTRACTOR) {
            throw new IllegalArgumentException("Los distractores deben cumplir con una longitud y estructura mínimas");
        }
        // Al menos una palabra con más de 2 letras
        boolean tieneWordSignificativa = Arrays.stream(trimmed.split("\\s+"))
                .anyMatch(w -> w.length() > 2);
        if (!tieneWordSignificativa) {
            throw new IllegalArgumentException("Los distractores deben cumplir con una longitud y estructura mínimas");
        }
    }

    /**
     * rechaza distractores idénticos entre sí y distractor igual a la
     * pregunta directa.
     */
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
        // Ningún distractor puede ser igual al enunciado de la pregunta directa
        String pd = pregunta.getPreguntaDirecta().trim().toLowerCase();
        for (String d : distractores) {
            if (d.toLowerCase().equals(pd)) {
                throw new IllegalArgumentException(
                        "Un distractor no puede ser igual al enunciado de la pregunta directa");
            }
        }
    }
}
