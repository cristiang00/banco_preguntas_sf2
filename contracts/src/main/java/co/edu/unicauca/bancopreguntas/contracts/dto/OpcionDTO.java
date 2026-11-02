package co.edu.unicauca.bancopreguntas.contracts.dto;

import java.io.Serializable;

public class OpcionDTO implements Serializable {
    private String clave;
    private String texto;

    public OpcionDTO() {}

    public OpcionDTO(String clave, String texto) {
        this.clave = clave;
        this.texto = texto;
    }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}

