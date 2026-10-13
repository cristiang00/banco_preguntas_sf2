package co.edu.unicauca.bancopreguntas.domain.evaluation;

import java.util.List;
import java.util.Set;

/**
 * Resultado de evaluar una respuesta mediante el patrón Template Method.
 */
public final class EvaluationResult {

    private final boolean valid;
    private final boolean correct;
    private final double score;
    private final List<String> selectedAnswers;
    private final Set<String> correctAnswers;
    private final String feedback;

    private EvaluationResult(boolean valid, boolean correct, double score, List<String> selectedAnswers,
                             Set<String> correctAnswers, String feedback) {
        this.valid = valid;
        this.correct = correct;
        this.score = score;
        this.selectedAnswers = List.copyOf(selectedAnswers);
        this.correctAnswers = Set.copyOf(correctAnswers);
        this.feedback = feedback;
    }

    /** Resultado de una respuesta válida comparada con las respuestas correctas. */
    public static EvaluationResult evaluated(boolean correct, double score, List<String> selectedAnswers,
                                             Set<String> correctAnswers, String feedback) {
        return new EvaluationResult(true, correct, score, selectedAnswers, correctAnswers, feedback);
    }

    /** Resultado de una respuesta inválida (rama No de validación). */
    public static EvaluationResult invalid(String errorMessage) {
        return new EvaluationResult(false, false, 0.0, List.of(), Set.of(), errorMessage);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean valid() {
        return valid;
    }

    public boolean isCorrect() {
        return correct;
    }

    public boolean correct() {
        return correct;
    }

    public double getScore() {
        return score;
    }

    public double score() {
        return score;
    }

    public List<String> getSelectedAnswers() {
        return selectedAnswers;
    }

    public Set<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public String getFeedback() {
        return feedback;
    }

    public String feedback() {
        return feedback;
    }

    @Override
    public String toString() {
        if (!valid) {
            return "NO VÁLIDA | " + feedback;
        }
        return (correct ? "CORRECTA" : "INCORRECTA") + " | puntaje " + score + " | " + feedback;
    }
}
