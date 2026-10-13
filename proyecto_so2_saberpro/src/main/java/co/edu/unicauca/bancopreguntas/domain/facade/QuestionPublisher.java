package co.edu.unicauca.bancopreguntas.domain.facade;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

import java.util.Objects;

/**
 * Subsistema de publicación invocado por la Facade para publicar una pregunta aprobada.
 */
public class QuestionPublisher {

    public void publish(Pregunta question) {
        Objects.requireNonNull(question, "La pregunta es obligatoria");
        question.publish();
    }
}
