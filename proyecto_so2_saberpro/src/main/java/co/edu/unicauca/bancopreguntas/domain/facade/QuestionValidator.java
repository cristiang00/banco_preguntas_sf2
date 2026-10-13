package co.edu.unicauca.bancopreguntas.domain.facade;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

import java.util.Objects;

/**
 * Subsistema de validación invocado por la Facade para verificar la consistencia
 * de una pregunta antes de guardarla o publicarla.
 */
public class QuestionValidator {

    public void validate(Pregunta question) {
        Objects.requireNonNull(question, "La pregunta es obligatoria");
        if (question.getPreguntaDirecta() == null || question.getPreguntaDirecta().isBlank()) {
            throw new IllegalArgumentException("El enunciado de la pregunta es obligatorio");
        }
        if (question.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de pregunta es obligatorio");
        }
    }
}
