package co.edu.unicauca.bancopreguntas.validation.rules;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class ReglaCamposObligatorios implements ReglaValidacion {

    @Override
    public List<ErrorValidacionDTO> validar(PreguntaDTO p) {
        List<ErrorValidacionDTO> errores = new ArrayList<>();

        if (p.getContexto() == null || p.getContexto().trim().isEmpty()) {
            errores.add(new ErrorValidacionDTO("contexto", "El contexto es obligatorio", "CAMPO_OBLIGATORIO_FALTANTE"));
        }

        if (p.getPreguntaDirecta() == null || p.getPreguntaDirecta().trim().isEmpty()) {
            errores.add(new ErrorValidacionDTO("preguntaDirecta", "La pregunta directa es obligatoria", "CAMPO_OBLIGATORIO_FALTANTE"));
        }

        if (p.getJustificacion() == null || p.getJustificacion().trim().isEmpty()) {
            errores.add(new ErrorValidacionDTO("justificacion", "La justificación es obligatoria", "CAMPO_OBLIGATORIO_FALTANTE"));
        }

        if (p.getOpciones() == null || p.getOpciones().isEmpty()) {
            errores.add(new ErrorValidacionDTO("opciones", "Las opciones de respuesta son obligatorias", "CAMPO_OBLIGATORIO_FALTANTE"));
        }

        return errores;
    }

    @Override
    public String getNombre() {
        return "Regla de Campos Obligatorios";
    }
}

