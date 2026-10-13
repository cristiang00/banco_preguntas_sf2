package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

/**
 * Rechazada: el revisor rechazó la pregunta. Vuelve a Borrador para realizar ajustes.
 */
public class RejectedState extends AbstractQuestionState {

    public static final String NAME = "Rechazada";

    @Override
    public void returnToDraft(Pregunta question) {
        question.setState(new DraftState());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EstadoPregunta getEstado() {
        return EstadoPregunta.RECHAZADA;
    }
}
