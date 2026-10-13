package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

/**
 * Aprobada: la pregunta fue aprobada por el revisor y está lista para publicarse.
 */
public class ApprovedState extends AbstractQuestionState {

    public static final String NAME = "Aprobada";

    @Override
    public void publish(Pregunta question) {
        question.setState(new PublishedState());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EstadoPregunta getEstado() {
        return EstadoPregunta.APROBADA;
    }
}
