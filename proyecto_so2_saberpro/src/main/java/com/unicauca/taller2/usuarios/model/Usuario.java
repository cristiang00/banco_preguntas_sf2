package com.unicauca.taller2.usuarios.model;

import java.time.LocalDateTime;

/**
 * Entidad que representa un usuario del sistema.
 */
public class Usuario {

    private final int id;
    private final String nombreUsuario;
    private final String nombreCompleto;
    private final Rol rol;
    private EstadoUsuario estado;
    private final String passwordHash;
    private final LocalDateTime fechaCreacion;

    /**
     * Constructor completo.
     *
     * @param id el identificador del usuario
     * @param nombreUsuario el nombre de usuario
     * @param nombreCompleto el nombre completo
     * @param rol el rol del usuario
     * @param estado el estado del usuario
     * @param passwordHash el hash de la contraseña
     * @param fechaCreacion la fecha de creación
     */
    public Usuario(int id, String nombreUsuario, String nombreCompleto,
                   Rol rol, EstadoUsuario estado, String passwordHash,
                   LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.estado = estado;
        this.passwordHash = passwordHash;
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Constructor para registro de nuevo usuario.
     *
     * @param nombreUsuario el nombre de usuario
     * @param nombreCompleto el nombre completo
     * @param rol el rol del usuario
     * @param estado el estado del usuario
     * @param passwordHash el hash de la contraseña
     */
    public Usuario(String nombreUsuario, String nombreCompleto,
                   Rol rol, EstadoUsuario estado, String passwordHash) {
        this(0, nombreUsuario, nombreCompleto, rol, estado, passwordHash, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Rol getRol() {
        return rol;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    /**
     * Modifica el estado del usuario.
     *
     * @param estado el nuevo estado del usuario
     */
    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombreUsuario='" + nombreUsuario + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", rol=" + rol +
                ", estado=" + estado +
                '}';
    }
}
