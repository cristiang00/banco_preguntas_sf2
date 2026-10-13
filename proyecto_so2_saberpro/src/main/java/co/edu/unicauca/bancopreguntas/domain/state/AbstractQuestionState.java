package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

/**
 * Estado base: por defecto niega todas las operaciones.
 * Las clases concretas sobrescriben únicamente las operaciones permitidas.
 */
public abstract class AbstractQuestionState implements QuestionState {

    @Override
    public void submitForReview(Pregunta question) {
        throw denied("enviar a revisión");
    }

    @Override
    public void assignReviewer(Pregunta question) {
        throw denied("asignar un revisor a");
    }

    @Override
    public void approve(Pregunta question) {
        throw denied("aprobar");
    }

    @Override
    public void reject(Pregunta question) {
        throw denied("rechazar");
    }

    @Override
    public void returnToDraft(Pregunta question) {
        throw denied("devolver a Borrador");
    }

    @Override
    public void publish(Pregunta question) {
        throw denied("publicar");
    }

    @Override
    public void archive(Pregunta question) {
        throw denied("archivar");
    }

    @Override
    public void ensureEditable(String action) {
        throw new IllegalStateException(String.format(
                "No se puede %s: la pregunta está en estado '%s' y solo es editable en estado 'Borrador'.",
                action, getName()));
    }

    @Override
    public void ensureUsableInTest(Pregunta question) {
        throw new IllegalStateException(String.format(
                "La pregunta no puede usarse en una prueba: está en estado '%s' y debe estar aprobada y publicada.",
                getName()));
    }

    @Override
    public boolean isUsableInTest() {
        return false;
    }

    /** Construye la excepción estándar para una transición no permitida (Tabla 2). */
    protected IllegalStateException denied(String operation) {
        return new IllegalStateException(String.format(
                "Transición no permitida: no se puede %s una pregunta en estado '%s'.", operation, getName()));
    }

    @Override
    public String toString() {
        return getName();
    }
}
