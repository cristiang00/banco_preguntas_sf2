package co.edu.unicauca.bancopreguntas.validation.rules;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.OpcionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(2)
public class ReglaRespuestaCorrecta implements ReglaValidacion {

    @Override
    public List<ErrorValidacionDTO> validar(PreguntaDTO p) {
        List<ErrorValidacionDTO> errores = new ArrayList<>();

        List<OpcionDTO> opciones = p.getOpciones();
        if (opciones == null || opciones.size() != 4) {
            errores.add(new ErrorValidacionDTO("opciones", "La pregunta debe tener exactamente 4 opciones de respuesta", "CANTIDAD_OPCIONES_INVALIDA"));
            return errores;
        }

        for (int i = 0; i < opciones.size(); i++) {
            OpcionDTO op = opciones.get(i);
            if (op == null || op.getTexto() == null || op.getTexto().trim().isEmpty()) {
                errores.add(new ErrorValidacionDTO("opciones[" + i + "]", "El texto de la opción no puede estar vacío", "OPCION_TEXTO_VACIO"));
            }
        }

        String respuestaCorrecta = p.getRespuestaCorrecta();
        if (respuestaCorrecta == null || respuestaCorrecta.trim().isEmpty()) {
            errores.add(new ErrorValidacionDTO("respuestaCorrecta", "Debe seleccionar exactamente una respuesta correcta", "RESPUESTA_CORRECTA_FALTANTE"));
            return errores;
        }

        String rc = respuestaCorrecta.trim();
        // Verificar que no contenga comas o múltiples claves
        if (rc.contains(",") || rc.split("\\s+").length > 1) {
            errores.add(new ErrorValidacionDTO("respuestaCorrecta", "Solo se permite una única respuesta correcta", "MULTIPLE_RESPUESTA_CORRECTA_NO_PERMITIDA"));
            return errores;
        }

        // Verificar que coincida con una de las opciones (por clave A,B,C,D o por texto)
        boolean coincide = opciones.stream().anyMatch(op -> 
            op != null && (rc.equalsIgnoreCase(op.getClave()) || rc.equalsIgnoreCase(op.getTexto()))
        );

        if (!coincide) {
            errores.add(new ErrorValidacionDTO("respuestaCorrecta", "La respuesta correcta seleccionada no coincide con ninguna de las 4 opciones", "RESPUESTA_CORRECTA_NO_COINCIDE"));
        }

        return errores;
    }

    @Override
    public String getNombre() {
        return "Regla de Respuesta Correcta Única";
    }
}

