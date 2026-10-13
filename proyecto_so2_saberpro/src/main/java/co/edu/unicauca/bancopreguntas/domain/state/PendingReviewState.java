package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

/**
 * Pendiente de revisión: la pregunta tiene contenido completo y espera asignación de revisor.
 */
public class PendingReviewState extends AbstractQuestionState {

    public static final String NAME = "Pendiente de revisión";

    @Override
    public void assignReviewer(Pregunta question) {
        question.setState(new ReviewState());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EstadoPregunta getEstado() {
        return EstadoPregunta.PENDIENTE_REVISION;
    }
}
