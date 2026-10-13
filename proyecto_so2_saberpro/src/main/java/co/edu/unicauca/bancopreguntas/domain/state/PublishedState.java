package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

/**
 * Publicada: la pregunta queda disponible para su uso en pruebas de evaluación.
 * Es el único estado en el que la pregunta puede usarse en una prueba.
 */
public class PublishedState extends AbstractQuestionState {

    public static final String NAME = "Publicada";

    @Override
    public void archive(Pregunta question) {
        question.setState(new ArchivedState());
    }

    @Override
    public void ensureUsableInTest(Pregunta question) {
        // Permitido: pregunta aprobada y publicada
    }

    @Override
    public boolean isUsableInTest() {
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EstadoPregunta getEstado() {
        return EstadoPregunta.PUBLICADA;
    }
}
