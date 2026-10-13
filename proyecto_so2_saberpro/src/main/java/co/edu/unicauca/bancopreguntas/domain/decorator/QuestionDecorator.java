package co.edu.unicauca.bancopreguntas.domain.decorator;

import java.util.Objects;

/**
 * Decorador abstracto que envuelve un {@link QuestionComponent} y delega sus llamadas.
 */
public abstract class QuestionDecorator implements QuestionComponent {

    protected final QuestionComponent question;

    protected QuestionDecorator(QuestionComponent question) {
        this.question = Objects.requireNonNull(question, "El componente a decorar es obligatorio");
    }

    @Override
    public String getDescription() {
        return question.getDescription();
    }

    @Override
    public double getScore() {
        return question.getScore();
    }

    protected static String requireDetail(String detail, String feature) {
        if (detail == null || detail.isBlank()) {
            throw new IllegalArgumentException("La característica '" + feature + "' requiere un detalle");
        }
        return detail.trim();
    }
}
