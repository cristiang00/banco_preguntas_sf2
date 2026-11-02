package co.edu.unicauca.bancopreguntas.contracts.dto;

import java.io.Serializable;

/**
 * Representa un error de validación estructural por campo (HU1 - SSCRUM-35).
 */
public class ErrorValidacionDTO implements Serializable {

    private String campo;
    private String mensaje;
    private String codigo;

    public ErrorValidacionDTO() {}

    public ErrorValidacionDTO(String campo, String mensaje, String codigo) {
        this.campo = campo;
        this.mensaje = mensaje;
        this.codigo = codigo;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}

