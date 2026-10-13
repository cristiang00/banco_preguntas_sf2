package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

/**
 * En revisión: el revisor analiza la pregunta y decide si la aprueba o rechaza.
 */
public class ReviewState extends AbstractQuestionState {

    public static final String NAME = "En revisión";

    @Override
    public void approve(Pregunta question) {
        question.setState(new ApprovedState());
    }

    @Override
    public void reject(Pregunta question) {
        question.setState(new RejectedState());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EstadoPregunta getEstado() {
        return EstadoPregunta.EN_REVISION;
    }
}
