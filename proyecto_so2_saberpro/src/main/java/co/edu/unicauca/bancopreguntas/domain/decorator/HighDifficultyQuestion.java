package co.edu.unicauca.bancopreguntas.domain.decorator;

/**
 * Decorador de alta dificultad: multiplica el puntaje por 1.5.
 */
public class HighDifficultyQuestion extends QuestionDecorator {

    public static final double SCORE_MULTIPLIER = 1.5;

    public HighDifficultyQuestion(QuestionComponent question) {
        super(question);
    }

    @Override
    public String getDescription() {
        return question.getDescription() + " [ALTA DIFICULTAD]";
    }

    @Override
    public double getScore() {
        return question.getScore() * SCORE_MULTIPLIER;
    }
}
