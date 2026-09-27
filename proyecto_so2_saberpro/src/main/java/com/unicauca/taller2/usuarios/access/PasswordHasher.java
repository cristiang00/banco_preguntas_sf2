package com.unicauca.taller2.usuarios.access;

/**
 * Interfaz para el hashing de contraseñas.
 */
public interface PasswordHasher {

    /**
     * Genera un hash seguro a partir de una contraseña en texto plano.
     *
     * @param rawPassword la contraseña en texto plano
     * @return el hash de la contraseña
     */
    String hash(String rawPassword);

    /**
     * Verifica si una contraseña en texto plano coincide con un hash almacenado.
     *
     * @param rawPassword la contraseña en texto plano
     * @param hash        el hash almacenado
     * @return true si la contraseña coincide con el hash, false en caso contrario
     */
    boolean verify(String rawPassword, String hash);
}
