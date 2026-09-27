package com.unicauca.taller2.usuarios.access;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación por defecto de la política de contraseñas.
 */
public class PasswordPolicyDefault implements PasswordPolicy {

    private static final int MIN_LENGTH = 6;

    @Override
    public void validar(String password) throws PasswordPolicyException {
        List<String> errores = new ArrayList<>();

        if (password == null || password.length() < MIN_LENGTH) {
            errores.add("La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }

        if (password != null) {
            if (!contieneDigito(password)) {
                errores.add("La contraseña debe contener al menos 1 dígito");
            }

            if (!contieneMayuscula(password)) {
                errores.add("La contraseña debe contener al menos 1 letra mayúscula");
            }

            if (!contieneCaracterEspecial(password)) {
                errores.add("La contraseña debe contener al menos 1 carácter especial");
            }
        }

        if (!errores.isEmpty()) {
            throw new PasswordPolicyException(String.join(". ", errores));
        }
    }

    private boolean contieneDigito(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean contieneMayuscula(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean contieneCaracterEspecial(String password) {
        for (char c : password.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }
}
