package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;

/**
 * Estado Borrador (GoF State Pattern).
 * Permite edición y el envío a revisión.
 */
public class DraftState extends AbstractQuestionState {

    @Override
    public void submitForReview(PreguntaCicloVidaContext context) {
        context.cambiarEstado(new PendingReviewState());
    }

    @Override
    public void ensureEditable() {
        // En estado Borrador la edición está permitida
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public String getName() {
        return "Borrador";
    }

    @Override
    public EstadoPreguntaDTO getEstadoDTO() {
        return EstadoPreguntaDTO.BORRADOR;
    }
}

