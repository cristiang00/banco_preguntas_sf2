package co.edu.unicauca.bancopreguntas.validation.controller;

import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.ResultadoValidacionDTO;
import co.edu.unicauca.bancopreguntas.validation.service.ValidationEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/validaciones")
@Tag(name = "Validaciones", description = "Validación estructural y reglas de calidad para preguntas Saber Pro")
public class ValidacionController {

    private final ValidationEngine validationEngine;

    public ValidacionController(ValidationEngine validationEngine) {
        this.validationEngine = validationEngine;
    }

    @PostMapping("/validar-pregunta")
    @Operation(summary = "Ejecutar validación estructural completa de una pregunta (HU1 - SSCRUM-35 a SSCRUM-40)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pregunta estructuralmente válida"),
        @ApiResponse(responseCode = "400", description = "Pregunta inválida con lista de errores por campo")
    })
    public ResponseEntity<ResultadoValidacionDTO> validarPregunta(@RequestBody PreguntaDTO pregunta) {
        ResultadoValidacionDTO resultado = validationEngine.validarPregunta(pregunta);
        if (resultado.isValido()) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
        }
    }
}

