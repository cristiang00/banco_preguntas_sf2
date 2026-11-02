package co.edu.unicauca.bancopreguntas.users.domain;

public enum Rol {
    ADMINISTRADOR,
    AUTOR,
    REVISOR,
    AUTOR_PREGUNTAS, // Compatibilidad con BD previa
    DOCENTE,
    ESTUDIANTE;

    public boolean esAutor() {
        return this == AUTOR || this == AUTOR_PREGUNTAS;
    }

    public boolean esAdministrador() {
        return this == ADMINISTRADOR;
    }

    public boolean esRevisor() {
        return this == REVISOR;
    }
}

