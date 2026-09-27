package com.unicauca.taller2.usuarios.services;

/**
 * Excepción lanzada cuando la autenticación falla.
 */
public class AutenticacionException extends RuntimeException {

    /**
     * Crea una nueva instancia de la excepción.
     *
     * @param message mensaje descriptivo
     */
    public AutenticacionException(String message) {
        super(message);
    }
}
