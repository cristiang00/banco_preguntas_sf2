package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

/**
 * Estado abstracto base que implementa el comportamiento por defecto de rechazar
 * transiciones no permitidas (GoF State Pattern).
 * Reutilizada y adaptada del proyecto original {@code AbstractQuestionState}.
 */
public abstract class AbstractQuestionState implements QuestionState {

    @Override
    public void submitForReview(PreguntaCicloVidaContext context) {
        throw new TransicionInvalidaException(
                "No se puede enviar a revisión una pregunta que se encuentra en estado '" + getName() + "'");
    }

    @Override
    public void approve(PreguntaCicloVidaContext context) {
        throw new TransicionInvalidaException(
                "No se puede aprobar una pregunta que se encuentra en estado '" + getName() + "'");
    }

    @Override
    public void reject(PreguntaCicloVidaContext context, String motivo) {
        throw new TransicionInvalidaException(
                "No se puede rechazar una pregunta que se encuentra en estado '" + getName() + "'");
    }

    @Override
    public void ensureEditable() {
        throw new TransicionInvalidaException(
                "Solo se pueden editar preguntas en estado Borrador. El estado actual es '" + getName() + "'");
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}
