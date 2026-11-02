package co.edu.unicauca.bancopreguntas.lifecycle.domain.state;

/**
 * Excepción cuando el usuario autenticado no es el autor de la pregunta (HU2 - SSCRUM-45).
 * Mapeada a HTTP 403 Forbidden.
 */
public class AccesoDenegadoException extends SecurityException {
    public AccesoDenegadoException(String message) {
        super(message);
    }
}

