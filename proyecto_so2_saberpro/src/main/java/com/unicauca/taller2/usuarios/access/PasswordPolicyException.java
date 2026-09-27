package com.unicauca.taller2.usuarios.access;

/**
 * Excepción lanzada cuando una contraseña no cumple con la política de seguridad.
 */
public class PasswordPolicyException extends RuntimeException {

    /**
     * Crea una nueva instancia de la excepción.
     *
     * @param message mensaje descriptivo indicando qué regla(s) no se cumplen
     */
    public PasswordPolicyException(String message) {
        super(message);
    }
}
