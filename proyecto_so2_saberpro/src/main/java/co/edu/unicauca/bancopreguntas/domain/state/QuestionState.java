package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;
import co.edu.unicauca.bancopreguntas.domain.entities.Pregunta;

/**
 * Estado del ciclo de vida de una {@link Pregunta} (patrón State).
 * <p>
 * Incluye las operaciones propuestas por la guía de laboratorio:
 * {@code submitForReview}, {@code approve}, {@code publish}, {@code archive}, {@code getName};
 * además de {@code assignReviewer}, {@code reject}, {@code returnToDraft} para cumplir
 * completamente con el flujo de la Figura 1 y los permisos de la Tabla 2.
 */
public interface QuestionState {

    /** Borrador → Pendiente de revisión. */
    void submitForReview(Pregunta question);

    /** Pendiente de revisión → En revisión. */
    void assignReviewer(Pregunta question);

    /** En revisión → Aprobada. */
    void approve(Pregunta question);

    /** En revisión → Rechazada. */
    void reject(Pregunta question);

    /** Rechazada → Borrador. */
    void returnToDraft(Pregunta question);

    /** Aprobada → Publicada. */
    void publish(Pregunta question);

    /** Publicada → Archivada. */
    void archive(Pregunta question);

    /**
     * Verifica que el contenido de la pregunta pueda modificarse en este estado.
     *
     * @param action descripción de la acción, usada en el mensaje de error
     */
    void ensureEditable(String action);

    /** Verifica que la pregunta pueda usarse en una prueba (solo Publicada). */
    void ensureUsableInTest(Pregunta question);

    /** @return {@code true} si la pregunta puede usarse en una prueba. */
    boolean isUsableInTest();

    /** @return nombre del estado en español (Figura 1). */
    String getName();

    /** @return el enum {@link EstadoPregunta} correspondiente para persistencia y UI. */
    EstadoPregunta getEstado();
}
