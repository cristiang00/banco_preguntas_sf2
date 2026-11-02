package co.edu.unicauca.bancopreguntas.contracts.dto;

import java.io.Serializable;

public class TokenResponseDTO implements Serializable {
    private String token;
    private String tipo = "Bearer";
    private Long usuarioId;
    private String nombreUsuario;
    private String nombreCompleto;
    private String rol;

    public TokenResponseDTO() {}

    public TokenResponseDTO(String token, Long usuarioId, String nombreUsuario, String nombreCompleto, String rol) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}

