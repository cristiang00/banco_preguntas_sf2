package co.edu.unicauca.bancopreguntas.domain.state;

import co.edu.unicauca.bancopreguntas.domain.entities.EstadoPregunta;

/**
 * Fábrica y conversor bidireccional entre {@link EstadoPregunta} y {@link QuestionState}.
 * Mantiene la separación entre la representación de persistencia/UI y las clases de comportamiento.
 */
public final class EstadosPregunta {

    private EstadosPregunta() {
    }

    /**
     * Construye la instancia de {@link QuestionState} que corresponde al valor del enum.
     */
    public static QuestionState desde(EstadoPregunta estado) {
        if (estado == null) {
            return new DraftState();
        }
        return switch (estado) {
            case BORRADOR -> new DraftState();
            case PENDIENTE_REVISION -> new PendingReviewState();
            case EN_REVISION -> new ReviewState();
            case APROBADA -> new ApprovedState();
            case RECHAZADA -> new RejectedState();
            case PUBLICADA -> new PublishedState();
            case ARCHIVADA -> new ArchivedState();
        };
    }

    /**
     * Resuelve el estado a partir de un nombre textual (en español o por nombre de enum).
     */
    public static QuestionState desdeNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return new DraftState();
        }
        String normalizado = nombre.trim();
        if (normalizado.equalsIgnoreCase("Borrador") || normalizado.equalsIgnoreCase("BORRADOR")) {
            return new DraftState();
        }
        if (normalizado.equalsIgnoreCase("Pendiente de revisión") || normalizado.equalsIgnoreCase("PENDIENTE_REVISION")) {
            return new PendingReviewState();
        }
        if (normalizado.equalsIgnoreCase("En revisión") || normalizado.equalsIgnoreCase("EN_REVISION")) {
            return new ReviewState();
        }
        if (normalizado.equalsIgnoreCase("Aprobada") || normalizado.equalsIgnoreCase("APROBADA")) {
            return new ApprovedState();
        }
        if (normalizado.equalsIgnoreCase("Rechazada") || normalizado.equalsIgnoreCase("RECHAZADA")) {
            return new RejectedState();
        }
        if (normalizado.equalsIgnoreCase("Publicada") || normalizado.equalsIgnoreCase("PUBLICADA")) {
            return new PublishedState();
        }
        if (normalizado.equalsIgnoreCase("Archivada") || normalizado.equalsIgnoreCase("ARCHIVADA")) {
            return new ArchivedState();
        }
        return new DraftState();
    }
}
