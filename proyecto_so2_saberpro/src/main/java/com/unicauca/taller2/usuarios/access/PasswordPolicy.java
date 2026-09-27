package com.unicauca.taller2.usuarios.access;

/**
 * Interfaz para la validación de políticas de contraseña.
 */
public interface PasswordPolicy {

    /**
     * Valida que la contraseña cumple con las reglas de seguridad.
     *
     * @param password la contraseña en texto plano a validar
     * @throws PasswordPolicyException si la contraseña no cumple alguna regla
     */
    void validar(String password) throws PasswordPolicyException;
}
