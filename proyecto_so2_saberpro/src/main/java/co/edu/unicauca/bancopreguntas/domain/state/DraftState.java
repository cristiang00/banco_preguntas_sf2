package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

/**
 * Borrador: la pregunta se crea y se guarda en este estado.
 * Es el único estado editable y permite enviarla a revisión cuando su contenido está completo.
 */
public class DraftState extends AbstractQuestionState {

    public static final String NAME = "Borrador";

    @Override
    public void submitForReview(Pregunta question) {
        if (!question.isContentComplete()) {
            throw new IllegalStateException("No se puede enviar a revisión: la pregunta no tiene su contenido "
                    + "completo (requiere al menos 2 opciones y " + question.getTipo().getReglaRespuestasCorrectas() + ").");
        }
        question.setState(new PendingReviewState());
    }

    @Override
    public void ensureEditable(String action) {
        // En Borrador el contenido es editable
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EstadoPregunta getEstado() {
        return EstadoPregunta.BORRADOR;
    }
}
