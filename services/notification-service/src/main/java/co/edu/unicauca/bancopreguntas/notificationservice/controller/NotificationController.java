package co.edu.unicauca.bancopreguntas.notificationservice.controller;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.EventParser;
import co.edu.unicauca.bancopreguntas.notificationservice.model.NotificacionEntity;
import co.edu.unicauca.bancopreguntas.notificationservice.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificaciones")
@Tag(name = "Notificaciones", description = "Endpoints para consulta y despacho de notificaciones con Factory Method y Adapter")
public class NotificationController {

    private final NotificacionService notificacionService;

    public NotificationController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    @Operation(summary = "Listar notificaciones registradas", description = "Permite filtrar por destinatario o listar todo el historial")
    public ResponseEntity<List<NotificacionEntity>> listarNotificaciones(
            @RequestParam(value = "destinatario", required = false) String destinatario) {
        if (destinatario != null && !destinatario.isBlank()) {
            return ResponseEntity.ok(notificacionService.listarPorDestinatario(destinatario));
        }
        return ResponseEntity.ok(notificacionService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar notificación por ID")
    public ResponseEntity<NotificacionEntity> buscarPorId(@PathVariable(value = "id") Long id) {
        return notificacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/eventos")
    @Operation(summary = "Ingestar y procesar evento de dominio para generación de notificaciones")
    public ResponseEntity<?> recibirEvento(@RequestBody Map<String, Object> payload) {
        try {
            DomainEvent event = EventParser.parse(payload);
            if (event != null) {
                List<NotificacionEntity> notificaciones = notificacionService.procesarEvento(event);
                return ResponseEntity.ok(Map.of(
                        "mensaje", "Evento procesado para notificaciones",
                        "tipo", event.getEventType(),
                        "notificacionesGeneradas", notificaciones.size()
                ));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Tipo de evento no reconocido o payload inválido"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
