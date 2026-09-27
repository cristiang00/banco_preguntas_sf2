package com.unicauca.taller2.usuarios.access;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Implementación de PasswordHasher usando el algoritmo Argon2id.
 */
public class Argon2PasswordHasher implements PasswordHasher {

    private static final int ITERATIONS = 3;
    private static final int MEMORY_KB = 65536; // 64 MB
    private static final int PARALLELISM = 1;

    private final Argon2 argon2;

    /**
     * Constructor que inicializa el hasher de Argon2id.
     */
    public Argon2PasswordHasher() {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    @Override
    public String hash(String rawPassword) {
        return argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, rawPassword.toCharArray());
    }

    @Override
    public boolean verify(String rawPassword, String hash) {
        return argon2.verify(hash, rawPassword.toCharArray());
    }
}
