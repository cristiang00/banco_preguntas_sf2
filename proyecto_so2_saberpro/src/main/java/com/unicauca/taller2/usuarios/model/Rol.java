package com.unicauca.taller2.usuarios.model;

/**
 * Enum que representa los roles disponibles en el sistema.
 */
public enum Rol {

    ADMINISTRADOR("Administrador"),
    AUTOR_PREGUNTAS("Autor de Preguntas"),
    REVISOR("Revisor"),
    DOCENTE("Docente"),
    ESTUDIANTE("Estudiante");

    private final String displayName;

    Rol(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
