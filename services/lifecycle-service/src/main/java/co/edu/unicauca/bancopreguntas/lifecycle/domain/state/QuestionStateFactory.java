package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;

public class QuestionStateFactory {

    public static QuestionState fromDTO(EstadoPreguntaDTO dto) {
        if (dto == null) return new DraftState();
        switch (dto) {
            case BORRADOR: return new DraftState();
            case PENDIENTE_REVISION: return new PendingReviewState();
            case APROBADA: return new ApprovedState();
            case RECHAZADA: return new RejectedState();
            default: return new DraftState();
        }
    }

    public static QuestionState fromString(String nombre) {
        if (nombre == null) return new DraftState();
        try {
            return fromDTO(EstadoPreguntaDTO.fromString(nombre));
        } catch (Exception e) {
            return new DraftState();
        }
    }
}

