package co.edu.unicauca.bancopreguntas.domain.evaluation;

/**
 * Indica que la respuesta del usuario no es válida para la pregunta
 * (rama "No" del paso "¿Respuesta válida?" del proceso de evaluación BPMN).
 */
public class InvalidAnswerException extends RuntimeException {

    public InvalidAnswerException(String message) {
        super(message);
    }
}
