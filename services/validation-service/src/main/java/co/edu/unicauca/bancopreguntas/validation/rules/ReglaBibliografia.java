package co.edu.unicauca.bancopreguntas.validation.rules;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(4)
public class ReglaBibliografia implements ReglaValidacion {

    @Override
    public List<ErrorValidacionDTO> validar(PreguntaDTO p) {
        List<ErrorValidacionDTO> errores = new ArrayList<>();

        String biblio = p.getBibliografia();
        if (biblio == null || biblio.trim().isEmpty()) {
            errores.add(new ErrorValidacionDTO("bibliografia", "La bibliografía es obligatoria y no puede contener únicamente espacios", "BIBLIOGRAFIA_INVALIDA"));
        }

        return errores;
    }

    @Override
    public String getNombre() {
        return "Regla de Bibliografía";
    }
}

