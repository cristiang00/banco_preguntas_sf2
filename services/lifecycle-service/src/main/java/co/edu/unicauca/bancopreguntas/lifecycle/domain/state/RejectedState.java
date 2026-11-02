package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;

/**
 * Estado Rechazada (GoF State Pattern).
 * Estado final asignado cuando un revisor rechaza la pregunta con observaciones.
 */
public class RejectedState extends AbstractQuestionState {

    @Override
    public String getName() {
        return "Rechazada";
    }

    @Override
    public EstadoPreguntaDTO getEstadoDTO() {
        return EstadoPreguntaDTO.RECHAZADA;
    }
}
