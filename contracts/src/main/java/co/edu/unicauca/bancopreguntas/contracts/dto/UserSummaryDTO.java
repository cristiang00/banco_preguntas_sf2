package co.edu.unicauca.bancopreguntas.contracts.dto;

import java.io.Serializable;

public class UserSummaryDTO implements Serializable {
    private Long id;
    private String nombreUsuario;
    private String nombreCompleto;
    private String rol;
    private String estado;
    private String email;

    public UserSummaryDTO() {}

    public UserSummaryDTO(Long id, String nombreUsuario, String nombreCompleto, String rol, String estado, String email) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.estado = estado;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

