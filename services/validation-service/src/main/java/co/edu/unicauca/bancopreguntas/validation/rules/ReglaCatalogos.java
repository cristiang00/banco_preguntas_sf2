package co.edu.unicauca.bancopreguntas.validation.rules;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.validation.client.CatalogClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(5)
public class ReglaCatalogos implements ReglaValidacion {

    private final CatalogClient catalogClient;

    public ReglaCatalogos(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    @Override
    public List<ErrorValidacionDTO> validar(PreguntaDTO p) {
        List<ErrorValidacionDTO> errores = new ArrayList<>();

        if (p.getCompetencia() == null || p.getCompetencia().trim().isEmpty()) {
            errores.add(new ErrorValidacionDTO("competencia", "Debe seleccionar una competencia del catálogo", "COMPETENCIA_FALTANTE"));
        } else if (!catalogClient.esCompetenciaValida(p.getCompetencia().trim())) {
            errores.add(new ErrorValidacionDTO("competencia", "La competencia seleccionada no existe en el catálogo", "COMPETENCIA_NO_EXISTE"));
        }

        if (p.getTema() == null || p.getTema().trim().isEmpty()) {
            errores.add(new ErrorValidacionDTO("tema", "Debe seleccionar un tema del catálogo", "TEMA_FALTANTE"));
        } else if (!catalogClient.esTemaValido(p.getTema().trim())) {
            errores.add(new ErrorValidacionDTO("tema", "El tema seleccionado no existe en el catálogo", "TEMA_NO_EXISTE"));
        }

        if (p.getSubtema() == null || p.getSubtema().trim().isEmpty()) {
            errores.add(new ErrorValidacionDTO("subtema", "Debe seleccionar un subtema del catálogo", "SUBTEMA_FALTANTE"));
        } else if (!catalogClient.esSubtemaValido(p.getSubtema().trim())) {
            errores.add(new ErrorValidacionDTO("subtema", "El subtema seleccionado no existe en el catálogo", "SUBTEMA_NO_EXISTE"));
        }

        return errores;
    }

    @Override
    public String getNombre() {
        return "Regla de Catálogos (Competencia, Tema, Subtema)";
    }
}

