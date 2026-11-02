package co.edu.unicauca.bancopreguntas.validation.rules;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Order(3)
public class ReglaDificultad implements ReglaValidacion {

    private static final Set<String> DIFICULTADES_VALIDAS = Set.of("BAJO", "MEDIO", "ALTO");

    @Override
    public List<ErrorValidacionDTO> validar(PreguntaDTO p) {
        List<ErrorValidacionDTO> errores = new ArrayList<>();

        String dificultad = p.getNivelDificultad();
        if (dificultad == null || dificultad.trim().isEmpty()) {
            errores.add(new ErrorValidacionDTO("nivelDificultad", "El nivel de dificultad es obligatorio", "DIFICULTAD_FALTANTE"));
            return errores;
        }

        if (!DIFICULTADES_VALIDAS.contains(dificultad.trim().toUpperCase())) {
            errores.add(new ErrorValidacionDTO("nivelDificultad", "Nivel de dificultad inválido. Valores permitidos: Bajo, Medio, Alto", "DIFICULTAD_INVALIDA"));
        }

        return errores;
    }

    @Override
    public String getNombre() {
        return "Regla de Nivel de Dificultad";
    }
}

