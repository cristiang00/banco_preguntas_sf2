package co.edu.unicauca.bancopreguntas.domain.decorator;

/**
 * Decorador para preguntas marcadas para revisión con su respectivo motivo.
 */
public class ReviewFlaggedQuestion extends QuestionDecorator {

    private final String reason;

    public ReviewFlaggedQuestion(QuestionComponent question, String reason) {
        super(question);
        this.reason = requireDetail(reason, "Marcada para revisión");
    }

    @Override
    public String getDescription() {
        return question.getDescription() + " [MARCADA PARA REVISIÓN: " + reason + "]";
    }
}
