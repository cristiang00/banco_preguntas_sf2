package co.edu.unicauca.bancopreguntas.review.application.ports.output;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;

import java.util.List;

/**
 * Puerto de salida para comunicación con el microservicio de Ciclo de Vida.
 */
public interface LifecycleClientPort {

    EstadoPreguntaDTO obtenerEstado(Long preguntaId);

    void aplicarDecision(Long preguntaId, String decision, Long revisorId, String emailAutor,
                         String contexto, List<String> observaciones, String correlationId);
}

