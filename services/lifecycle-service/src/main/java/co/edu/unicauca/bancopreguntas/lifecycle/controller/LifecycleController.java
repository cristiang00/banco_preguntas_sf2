package co.edu.unicauca.bancopreguntas.lifecycle.controller;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.state.AccesoDenegadoException;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.state.TransicionInvalidaException;
import co.edu.unicauca.bancopreguntas.lifecycle.service.LifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Ciclo de Vida", description = "Gestión de estados, patrón State y transiciones de preguntas")
public class LifecycleController {

    private final LifecycleService lifecycleService;

    public LifecycleController(LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @PutMapping("/preguntas/{id}/enviar-revision")
    @Operation(summary = "Enviar pregunta a revisión (HU2 - SSCRUM-42 a SSCRUM-45)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pregunta enviada a revisión exitosamente"),
        @ApiResponse(responseCode = "403", description = "El usuario no es el autor de la pregunta"),
        @ApiResponse(responseCode = "409", description = "Transición inválida: la pregunta no está en Borrador")
    })
    public ResponseEntity<?> enviarARevision(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "Autor") String userName,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            String contexto = (body != null && body.containsKey("contexto")) ? String.valueOf(body.get("contexto")) : "Pregunta #" + id;
            String preguntaDirecta = (body != null && body.containsKey("preguntaDirecta")) ? String.valueOf(body.get("preguntaDirecta")) : "";

            EstadoPreguntaDTO nuevoEstado = lifecycleService.enviarARevision(
                    id, userId, userName, contexto, preguntaDirecta, correlationId
            );

            return ResponseEntity.ok(Map.of(
                    "preguntaId", id,
                    "estado", nuevoEstado.name(),
                    "estadoLabel", nuevoEstado.getLabel(),
                    "estadoColor", nuevoEstado.getColorHex(),
                    "mensaje", "Pregunta enviada exitosamente a revisión"
            ));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (TransicionInvalidaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/ciclo-vida/preguntas/{id}/estado")
    @Operation(summary = "Consultar estado actual y color de una pregunta (HU2 - SSCRUM-44)")
    public ResponseEntity<?> obtenerEstado(@PathVariable Long id) {
        EstadoPreguntaDTO estado = lifecycleService.obtenerEstado(id);
        return ResponseEntity.ok(Map.of(
                "preguntaId", id,
                "estado", estado.name(),
                "label", estado.getLabel(),
                "color", estado.getColorHex()
        ));
    }

    @GetMapping("/ciclo-vida/preguntas/{id}/puede-editar")
    @Operation(summary = "Verificar si la pregunta puede ser editada (HU2 - SSCRUM-46)")
    public ResponseEntity<?> puedeEditar(@PathVariable Long id) {
        boolean editable = lifecycleService.puedeEditar(id);
        return ResponseEntity.ok(Map.of(
                "preguntaId", id,
                "puedeEditar", editable
        ));
    }

    @PostMapping("/ciclo-vida/preguntas/{id}/inicializar")
    @Operation(summary = "Inicializar ciclo de vida de una nueva pregunta en Borrador")
    public ResponseEntity<?> inicializarPregunta(
            @PathVariable Long id,
            @RequestParam Long autorId) {
        lifecycleService.registrarPreguntaInicial(id, autorId);
        return ResponseEntity.ok(Map.of("preguntaId", id, "estado", EstadoPreguntaDTO.BORRADOR.name()));
    }

    @PutMapping("/ciclo-vida/preguntas/{id}/decision")
    @Operation(summary = "Aplicar decisión de revisores (Aprobada / Rechazada) coordinada desde Review Service")
    public ResponseEntity<?> aplicarDecision(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestBody Map<String, Object> body) {
        try {
            String decision = (String) body.get("decision");
            String autorEmail = (String) body.get("autorEmail");
            String contexto = (String) body.get("contexto");
            @SuppressWarnings("unchecked")
            List<String> observaciones = (List<String>) body.get("observaciones");

            EstadoPreguntaDTO nuevoEstado = lifecycleService.aplicarDecisionRevisor(
                    id, decision, userId, autorEmail, contexto, observaciones, correlationId
            );

            return ResponseEntity.ok(Map.of(
                    "preguntaId", id,
                    "estado", nuevoEstado.name(),
                    "estadoLabel", nuevoEstado.getLabel(),
                    "estadoColor", nuevoEstado.getColorHex()
            ));
        } catch (TransicionInvalidaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

