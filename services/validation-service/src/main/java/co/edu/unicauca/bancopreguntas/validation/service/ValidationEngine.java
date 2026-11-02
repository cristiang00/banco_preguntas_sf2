package co.edu.unicauca.bancopreguntas.validation.service;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.ResultadoValidacionDTO;
import co.edu.unicauca.bancopreguntas.validation.domain.ResultadoValidacionEntity;
import co.edu.unicauca.bancopreguntas.validation.repository.ResultadoValidacionRepository;
import co.edu.unicauca.bancopreguntas.validation.rules.ReglaValidacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidationEngine {

    private final List<ReglaValidacion> reglas;
    private final ResultadoValidacionRepository resultadoRepository;

    public ValidationEngine(List<ReglaValidacion> reglas, ResultadoValidacionRepository resultadoRepository) {
        this.reglas = reglas;
        this.resultadoRepository = resultadoRepository;
    }

    @Transactional
    public ResultadoValidacionDTO validarPregunta(PreguntaDTO pregunta) {
        if (pregunta == null) {
            return ResultadoValidacionDTO.conErrores(List.of(
                    new ErrorValidacionDTO("pregunta", "El cuerpo de la pregunta es nulo", "CUERPO_NULO")
            ));
        }

        List<ErrorValidacionDTO> todosLosErrores = new ArrayList<>();
        for (ReglaValidacion regla : reglas) {
            List<ErrorValidacionDTO> erroresRegla = regla.validar(pregunta);
            if (erroresRegla != null && !erroresRegla.isEmpty()) {
                todosLosErrores.addAll(erroresRegla);
            }
        }

        boolean esValido = todosLosErrores.isEmpty();
        String detalle = todosLosErrores.stream()
                .map(e -> e.getCampo() + ": " + e.getMensaje())
                .collect(Collectors.joining("; "));

        if (detalle.length() > 1900) {
            detalle = detalle.substring(0, 1900) + "...";
        }

        ResultadoValidacionEntity registro = new ResultadoValidacionEntity(
                pregunta.getId(),
                esValido,
                todosLosErrores.size(),
                detalle
        );
        resultadoRepository.save(registro);

        return new ResultadoValidacionDTO(esValido, todosLosErrores);
    }
}

