package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;

/**
 * Estado Rechazada (GoF State Pattern).
 * Estado asignado cuando un revisor rechaza la pregunta con observaciones.
 * Permite la transición a Borrador al crear una nueva versión mejorada (HU6.S3).
 */
public class RejectedState extends AbstractQuestionState {

    @Override
    public void createNewVersion(PreguntaCicloVidaContext context) {
        context.cambiarEstado(new DraftState());
    }

    @Override
    public String getName() {
        return "Rechazada";
    }

    @Override
    public EstadoPreguntaDTO getEstadoDTO() {
        return EstadoPreguntaDTO.RECHAZADA;
    }
}
