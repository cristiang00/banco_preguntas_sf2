package co.edu.unicauca.bancopreguntas.review.controller;

import co.edu.unicauca.bancopreguntas.contracts.dto.AsignarRevisoresRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.DecisionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.EvaluacionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.review.client.QuestionsServiceClient;
import co.edu.unicauca.bancopreguntas.review.domain.EvaluacionRevisorEntity;
import co.edu.unicauca.bancopreguntas.review.domain.ObservacionEntity;
import co.edu.unicauca.bancopreguntas.review.domain.RevisionEntity;
import co.edu.unicauca.bancopreguntas.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/revisiones")
@Tag(name = "Revisiones", description = "Asignación de revisores, listado asignado, evaluaciones y decisiones con consenso")
public class ReviewController {

    private final ReviewService reviewService;
    private final QuestionsServiceClient questionsClient;

    public ReviewController(ReviewService reviewService, QuestionsServiceClient questionsClient) {
        this.reviewService = reviewService;
        this.questionsClient = questionsClient;
    }

    @PostMapping("/asignar")
    @Operation(summary = "Asignar entre 1 y 3 revisores a una pregunta en revisión (HU4 - SSCRUM-56 a SSCRUM-61)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Asignación exitosa y evento publicado"),
        @ApiResponse(responseCode = "400", description = "Error en validación: 0 o más de 3 revisores, autor asignado, o revisor inexistente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo Administradores pueden asignar revisores"),
        @ApiResponse(responseCode = "409", description = "La pregunta no se encuentra en estado Pendiente de revisión")
    })
    public ResponseEntity<?> asignarRevisores(
            @RequestBody AsignarRevisoresRequestDTO request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMINISTRADOR") String userRole,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        if (!"ADMINISTRADOR".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los administradores pueden asignar revisores"));
        }
        try {
            List<RevisionEntity> creadas = reviewService.asignarRevisores(
                    request.getPreguntaId(), request.getRevisorIds(), userId, correlationId
            );
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Revisores asignados correctamente",
                    "totalAsignados", creadas.size(),
                    "asignaciones", creadas
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Consultar listado de preguntas pendientes de revisión para el Administrador (HU4)")
    public ResponseEntity<List<PreguntaDTO>> listarPendientes() {
        List<PreguntaDTO> pendientes = questionsClient.listarPreguntasPendientes();
        List<PreguntaDTO> sinAsignar = pendientes.stream()
                .filter(p -> !reviewService.tieneRevisoresAsignados(p.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(sinAsignar);
    }

    @GetMapping("/asignadas")
    @Operation(summary = "Listar preguntas asignadas al revisor autenticado con paginación y filtros (HU5 - SSCRUM-62)")
    public ResponseEntity<?> listarAsignadas(
            @RequestHeader(value = "X-User-Id", defaultValue = "6") Long revisorId,
            @RequestHeader(value = "X-User-Role", defaultValue = "REVISOR") String userRole,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "dificultad", required = false) String dificultad,
            @RequestParam(value = "competencia", required = false) String competencia) {
        if (!"REVISOR".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acceso exclusivo para usuarios con rol Revisor"));
        }
        return ResponseEntity.ok(reviewService.listarAsignadas(revisorId, page, size, dificultad, competencia));
    }

    @PostMapping("/{id}/evaluacion")
    @Operation(summary = "Registrar evaluación y observaciones de una pregunta asignada (HU5 - SSCRUM-63)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evaluación registrada exitosamente"),
        @ApiResponse(responseCode = "403", description = "La pregunta no está asignada al revisor autenticado"),
        @ApiResponse(responseCode = "409", description = "La revisión ya fue finalizada")
    })
    public ResponseEntity<?> registrarEvaluacion(
            @PathVariable(value = "id") Long id,
            @RequestBody EvaluacionRequestDTO request,
            @RequestHeader(value = "X-User-Id", defaultValue = "6") Long revisorId) {
        try {
            EvaluacionRevisorEntity eval = reviewService.registrarEvaluacion(id, request, revisorId);
            return ResponseEntity.ok(eval);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/decision")
    @Operation(summary = "Registrar decisión final (Aprobar o Rechazar) con validación de observaciones y consenso (HU5 - SSCRUM-64 a SSCRUM-66)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Decisión registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Al rechazar debe existir al menos una observación no vacía"),
        @ApiResponse(responseCode = "403", description = "La revisión no pertenece al revisor autenticado"),
        @ApiResponse(responseCode = "409", description = "La revisión ya fue decidida previamente")
    })
    public ResponseEntity<?> registrarDecision(
            @PathVariable(value = "id") Long id,
            @RequestBody DecisionRequestDTO request,
            @RequestHeader(value = "X-User-Id", defaultValue = "6") Long revisorId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        try {
            RevisionEntity rev = reviewService.registrarDecision(id, request, revisorId, correlationId);
            return ResponseEntity.ok(Map.of(
                    "revisionId", rev.getId(),
                    "estadoRevision", rev.getEstado().name(),
                    "mensaje", "Decisión registrada correctamente"
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pregunta/{preguntaId}/observaciones")
    @Operation(summary = "Consultar observaciones de revisores visibles para el autor")
    public ResponseEntity<List<ObservacionEntity>> listarObservaciones(@PathVariable(value = "preguntaId") Long preguntaId) {
        return ResponseEntity.ok(reviewService.listarObservaciones(preguntaId));
    }
}
