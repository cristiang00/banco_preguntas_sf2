package com.unicauca.taller2.usuarios.model;

/**
 * Enum que representa el estado de un usuario en el sistema.
 */
public enum EstadoUsuario {

    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String displayName;

    EstadoUsuario(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
