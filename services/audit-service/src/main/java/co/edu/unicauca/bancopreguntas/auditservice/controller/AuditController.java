package co.edu.unicauca.bancopreguntas.auditservice.controller;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.EventParser;
import co.edu.unicauca.bancopreguntas.auditservice.model.RegistroAuditoriaEntity;
import co.edu.unicauca.bancopreguntas.auditservice.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auditoria")
@Tag(name = "Auditoría", description = "Endpoints para consulta de la bitácora de auditoría y trazabilidad orientada a eventos")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar eventos auditados", description = "Obtiene la bitácora global de eventos ordenada cronológicamente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bitácora de auditoría"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo ADMINISTRADOR")
    })
    public ResponseEntity<List<RegistroAuditoriaEntity>> listarAuditoria(
            @RequestParam(value = "eventType", required = false) String eventType) {
        if (eventType != null && !eventType.isBlank()) {
            return ResponseEntity.ok(auditService.listarPorTipoEvento(eventType));
        }
        return ResponseEntity.ok(auditService.listarTodos());
    }

    @GetMapping("/pregunta/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Historial de auditoría por pregunta", description = "Recupera la trazabilidad completa de cambios de estado y decisiones sobre una pregunta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historial auditado"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo ADMINISTRADOR")
    })
    public ResponseEntity<List<RegistroAuditoriaEntity>> listarPorPregunta(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(auditService.listarPorPregunta(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Consultar registro de auditoría específico por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registro encontrado"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo ADMINISTRADOR"),
        @ApiResponse(responseCode = "404", description = "Registro no encontrado")
    })
    public ResponseEntity<RegistroAuditoriaEntity> buscarPorId(@PathVariable(value = "id") Long id) {
        return auditService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/eventos")
    @PreAuthorize("hasAnyRole('AUTOR', 'REVISOR', 'ADMINISTRADOR')")
    @Operation(summary = "Ingestar y procesar evento de dominio para registro en bitácora de auditoría")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento auditado"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<?> recibirEvento(@RequestBody Map<String, Object> payload) {
        try {
            DomainEvent event = EventParser.parse(payload);
            if (event != null) {
                Optional<RegistroAuditoriaEntity> reg = auditService.registrarEvento(event);
                return ResponseEntity.ok(Map.of(
                        "mensaje", "Evento auditado exitosamente",
                        "tipo", event.getEventType(),
                        "registrado", reg.isPresent()
                ));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Tipo de evento no reconocido o payload inválido"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
