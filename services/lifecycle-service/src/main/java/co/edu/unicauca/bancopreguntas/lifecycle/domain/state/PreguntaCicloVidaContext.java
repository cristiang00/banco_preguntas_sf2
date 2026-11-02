package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;

/**
 * Contexto del patrón State que contiene y delega las operaciones del ciclo de vida.
 */
public interface PreguntaCicloVidaContext {
    void cambiarEstado(QuestionState nuevoEstado);
    Long getPreguntaId();
    Long getAutorId();
}

