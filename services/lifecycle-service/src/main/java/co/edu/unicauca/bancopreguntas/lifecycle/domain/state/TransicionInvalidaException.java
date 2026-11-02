package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

/**
 * Excepción lanzada cuando una transición de estado no está permitida por el patrón State (HU2 - SSCRUM-43).
 * Mapeada a HTTP 409 Conflict.
 */
public class TransicionInvalidaException extends IllegalStateException {
    public TransicionInvalidaException(String message) {
        super(message);
    }
}

