package co.edu.unicauca.bancopreguntas.notificationservice.controller;

import co.edu.unicauca.bancopreguntas.notificationservice.model.NotificacionEntity;
import co.edu.unicauca.bancopreguntas.notificationservice.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(required = false) String destinatario) {
        if (destinatario != null && !destinatario.isBlank()) {
            return ResponseEntity.ok(notificacionService.listarPorDestinatario(destinatario));
        }
        return ResponseEntity.ok(notificacionService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar notificación por ID")
    public ResponseEntity<NotificacionEntity> buscarPorId(@PathVariable Long id) {
        return notificacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
