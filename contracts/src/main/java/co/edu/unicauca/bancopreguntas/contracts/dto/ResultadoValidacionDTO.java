package co.edu.unicauca.bancopreguntas.contracts.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado estructurado de la validación estructural de una pregunta (HU1 - SSCRUM-35, SSCRUM-36).
 */
public class ResultadoValidacionDTO implements Serializable {

    private boolean valido;
    private List<ErrorValidacionDTO> errores = new ArrayList<>();

    public ResultadoValidacionDTO() {}

    public ResultadoValidacionDTO(boolean valido, List<ErrorValidacionDTO> errores) {
        this.valido = valido;
        if (errores != null) {
            this.errores = errores;
        }
    }

    public static ResultadoValidacionDTO exitoso() {
        return new ResultadoValidacionDTO(true, new ArrayList<>());
    }

    public static ResultadoValidacionDTO conErrores(List<ErrorValidacionDTO> errores) {
        return new ResultadoValidacionDTO(false, errores);
    }

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public List<ErrorValidacionDTO> getErrores() {
        return errores;
    }

    public void setErrores(List<ErrorValidacionDTO> errores) {
        this.errores = errores;
    }
}

