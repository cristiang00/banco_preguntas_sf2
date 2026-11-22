package co.edu.unicauca.bancopreguntas.review.application.ports.output;

import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para comunicación con el microservicio de Preguntas.
 */
public interface PreguntasClientPort {

    Optional<PreguntaDTO> obtenerPregunta(Long preguntaId);

    void actualizarProyeccionEstado(Long preguntaId, String nuevoEstado);

    List<PreguntaDTO> listarPreguntasPendientes();
}

