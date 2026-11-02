package co.edu.unicauca.bancopreguntas.frontend.client;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import java.util.List;

public class ApiException extends Exception {

    private final int statusCode;
    private final String responseBody;
    private final List<ErrorValidacionDTO> erroresValidacion;

    public ApiException(int statusCode, String message, String responseBody, List<ErrorValidacionDTO> erroresValidacion) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.erroresValidacion = erroresValidacion != null ? erroresValidacion : List.of();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public List<ErrorValidacionDTO> getErroresValidacion() {
        return erroresValidacion;
    }
}
