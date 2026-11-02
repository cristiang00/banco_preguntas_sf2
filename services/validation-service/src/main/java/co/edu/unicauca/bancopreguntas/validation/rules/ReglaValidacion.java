package co.edu.unicauca.bancopreguntas.validation.rules;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;

import java.util.List;

/**
 * Interfaz para reglas de validación estructurales y de contenido (HU1 - SSCRUM-35).
 */
public interface ReglaValidacion {

    /**
     * Evalúa la regla sobre el DTO de la pregunta y retorna la lista de errores encontrados.
     *
     * @param pregunta DTO con los datos de la pregunta
     * @return lista de errores (vacía si es válida)
     */
    List<ErrorValidacionDTO> validar(PreguntaDTO pregunta);

    /**
     * Nombre descriptivo de la regla.
     */
    String getNombre();
}

