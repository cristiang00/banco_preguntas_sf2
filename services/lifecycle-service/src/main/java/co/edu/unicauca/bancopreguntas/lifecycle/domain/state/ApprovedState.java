package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;

/**
 * Estado Aprobada (GoF State Pattern).
 * Estado final de una pregunta validada y aceptada por todos los revisores.
 */
public class ApprovedState extends AbstractQuestionState {

    @Override
    public String getName() {
        return "Aprobada";
    }

    @Override
    public EstadoPreguntaDTO getEstadoDTO() {
        return EstadoPreguntaDTO.APROBADA;
    }
}

