package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;

/**
 * Interfaz de Estado para el ciclo de vida de una pregunta (GoF State Pattern).
 * Reutilizada y adaptada del proyecto original {@code co.edu.unicauca.bancopreguntas.domain.state.QuestionState}.
 */
public interface QuestionState {

    /**
     * Enviar a revisión (Borrador -> Pendiente de revisión).
     */
    void submitForReview(PreguntaCicloVidaContext context);

    /**
     * Aprobar la pregunta (Pendiente de revisión -> Aprobada).
     */
    void approve(PreguntaCicloVidaContext context);

    /**
     * Rechazar la pregunta (Pendiente de revisión -> Rechazada).
     */
    void reject(PreguntaCicloVidaContext context, String motivo);

    /**
     * Valida si la pregunta puede ser modificada/editada en este estado.
     * Solo permitido en estado Borrador (SSCRUM-46).
     */
    void ensureEditable();

    /**
     * Indica si el estado actual permite edición de contenido.
     */
    boolean isEditable();

    /**
     * Nombre descriptivo del estado.
     */
    String getName();

    /**
     * DTO del estado con su etiqueta y código de color (SSCRUM-44).
     */
    EstadoPreguntaDTO getEstadoDTO();
}
