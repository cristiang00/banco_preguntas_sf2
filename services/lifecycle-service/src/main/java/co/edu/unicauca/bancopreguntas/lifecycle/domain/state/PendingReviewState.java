package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;

/**
 * Estado Pendiente de Revisión (GoF State Pattern).
 * Bloquea edición (SSCRUM-46), permite aprobación o rechazo con evaluación.
 */
public class PendingReviewState extends AbstractQuestionState {

    @Override
    public void approve(PreguntaCicloVidaContext context) {
        context.cambiarEstado(new ApprovedState());
    }

    @Override
    public void reject(PreguntaCicloVidaContext context, String motivo) {
        context.cambiarEstado(new RejectedState());
    }

    @Override
    public String getName() {
        return "Pendiente de revisión";
    }

    @Override
    public EstadoPreguntaDTO getEstadoDTO() {
        return EstadoPreguntaDTO.PENDIENTE_REVISION;
    }
}

