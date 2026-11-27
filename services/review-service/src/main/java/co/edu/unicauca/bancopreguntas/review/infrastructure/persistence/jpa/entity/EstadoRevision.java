package co.edu.unicauca.bancopreguntas.review.infrastructure.persistence.jpa.entity;

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

