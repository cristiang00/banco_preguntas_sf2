package co.edu.unicauca.bancopreguntas.domain.decorator;

/**
 * Características adicionales aplicables a una pregunta mediante el patrón Decorator.
 */
public enum Feature {
    HIGH_DIFFICULTY("Alta dificultad", false),
    CONTEXTUALIZED("Contextualizada", true),
    FEEDBACK("Con retroalimentación", true),
    COMPETENCY("Asociada a competencia", true),
    REVIEW_FLAGGED("Marcada para revisión", true);

    private final String label;
    private final boolean requiresDetail;

    Feature(String label, boolean requiresDetail) {
        this.label = label;
        this.requiresDetail = requiresDetail;
    }

    public String getLabel() {
        return label;
    }

    public boolean requiresDetail() {
        return requiresDetail;
    }

    @Override
    public String toString() {
        return label;
    }
}
