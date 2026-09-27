package co.edu.unicauca.bancopreguntas.domain.services;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;
import co.edu.unicauca.bancopreguntas.domain.repositories.PreguntaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PreguntaService {
    private final PreguntaRepository preguntaRepository;

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
        Optional<Pregunta> opt = preguntaRepository.buscarPorId(preguntaId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("La pregunta no existe");
        }
        Pregunta pregunta = opt.get();
        
        if (pregunta.getAutorId() != autorId) {
            throw new IllegalArgumentException("No tiene permisos para modificar el estado de esta pregunta");
        }
        
        if (pregunta.getEstado() != EstadoPregunta.BORRADOR) {
            throw new IllegalArgumentException("Solo las preguntas en estado Borrador pueden enviarse a revisión");
        }

        preguntaRepository.actualizarEstado(preguntaId, EstadoPregunta.PENDIENTE_REVISION);
    }

    public List<Pregunta> listarMisPreguntas(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad, int offset, int limit) {
        return preguntaRepository.listarPorAutor(autorId, estadosFiltro, nivelDificultad, offset, limit);
    }

    public int contarMisPreguntas(int autorId, List<EstadoPregunta> estadosFiltro, String nivelDificultad) {
        return preguntaRepository.contarPorAutor(autorId, estadosFiltro, nivelDificultad);
    }

    public Pregunta obtenerPregunta(int preguntaId) {
        return preguntaRepository.buscarPorId(preguntaId).orElse(null);
    }

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

        // RF-12: Validación de expresiones prohibidas
        validarExpresionesProhibidas(pregunta.getPreguntaDirecta());
        validarExpresionesProhibidas(pregunta.getDistractor1());
        validarExpresionesProhibidas(pregunta.getDistractor2());
        validarExpresionesProhibidas(pregunta.getDistractor3());
        validarExpresionesProhibidas(pregunta.getDistractor4());

        // RF-13: Validación de longitud/estructura mínima de distractores
        validarEstructuraDistractor(pregunta.getDistractor1());
        validarEstructuraDistractor(pregunta.getDistractor2());
        validarEstructuraDistractor(pregunta.getDistractor3());
        validarEstructuraDistractor(pregunta.getDistractor4());
    }

    private void validarExpresionesProhibidas(String texto) {
        if (texto == null) return;
        String lower = texto.toLowerCase();
        lower = lower.replaceAll("[áàäâã]", "a")
                     .replaceAll("[éèëê]", "e")
                     .replaceAll("[íìïî]", "i")
                     .replaceAll("[óòöôõ]", "o")
                     .replaceAll("[úùüû]", "u");
        
        if (lower.contains("todas las anteriores") || lower.contains("ninguna de las anteriores")) {
            throw new IllegalArgumentException("No se permiten expresiones como 'Todas las anteriores' o 'Ninguna de las anteriores'");
        }
    }

    private void validarEstructuraDistractor(String distractor) {
        if (distractor == null) return;
        String trimmed = distractor.trim();
        // Nota: Se cubre longitud mínima y no-vacío como interpretación razonable de RF-13 
        // para este corte. Una validación gramatical profunda no es automatizable con certeza aquí.
        final int MIN_LONGITUD_DISTRACTOR = 3; 
        if (trimmed.length() < MIN_LONGITUD_DISTRACTOR) {
            throw new IllegalArgumentException("Los distractores deben cumplir con una longitud y estructura mínimas");
        }
    }
}
