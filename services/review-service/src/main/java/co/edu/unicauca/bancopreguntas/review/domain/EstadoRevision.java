package co.edu.unicauca.bancopreguntas.review.domain;

public enum EstadoRevision {
    ASIGNADA,
    EVALUADA,
    APROBADA,
    RECHAZADA,
    CERRADA_POR_RECHAZO;

    public boolean estaFinalizada() {
        return this == APROBADA || this == RECHAZADA || this == CERRADA_POR_RECHAZO;
    }
}

