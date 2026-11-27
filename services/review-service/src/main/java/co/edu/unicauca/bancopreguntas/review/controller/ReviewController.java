package co.edu.unicauca.bancopreguntas.review.controller;

import co.edu.unicauca.bancopreguntas.contracts.dto.AsignarRevisoresRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.DecisionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.EvaluacionRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.PreguntasClientPort;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.RevisionRepositoryPort;
import co.edu.unicauca.bancopreguntas.review.application.usecases.AsignarRevisoresUseCase;
import co.edu.unicauca.bancopreguntas.review.application.usecases.ConsultarHistorialUseCase;
import co.edu.unicauca.bancopreguntas.review.application.usecases.ListarAsignadasUseCase;
import co.edu.unicauca.bancopreguntas.review.application.usecases.RegistrarDecisionUseCase;
import co.edu.unicauca.bancopreguntas.review.application.usecases.RegistrarEvaluacionUseCase;
import co.edu.unicauca.bancopreguntas.review.domain.model.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.model.Evaluacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Observacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;
import co.edu.unicauca.bancopreguntas.review.infrastructure.security.ResourceServerSecurityConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST delgado (HU8.S2.3) con autorización basada en roles vía Spring Security @PreAuthorize (HU7.S4.2).
 * Delega toda la lógica de negocio a los casos de uso y puertos hexagonales sin leer cabeceras X-User-* sin verificar.
 */
@RestController
@RequestMapping("/revisiones")
@Tag(name = "Revisiones", description = "Endpoints para el flujo de revisión por pares, asignación, evaluación e historial")
public class ReviewController {

    private final AsignarRevisoresUseCase asignarRevisoresUseCase;
    private final ListarAsignadasUseCase listarAsignadasUseCase;
    private final RegistrarEvaluacionUseCase registrarEvaluacionUseCase;
    private final RegistrarDecisionUseCase registrarDecisionUseCase;
    private final ConsultarHistorialUseCase consultarHistorialUseCase;
    private final RevisionRepositoryPort repositoryPort;
    private final PreguntasClientPort preguntasClientPort;

    public ReviewController(AsignarRevisoresUseCase asignarRevisoresUseCase,
                            ListarAsignadasUseCase listarAsignadasUseCase,
                            RegistrarEvaluacionUseCase registrarEvaluacionUseCase,
                            RegistrarDecisionUseCase registrarDecisionUseCase,
                            ConsultarHistorialUseCase consultarHistorialUseCase,
                            RevisionRepositoryPort repositoryPort,
                            PreguntasClientPort preguntasClientPort) {
        this.asignarRevisoresUseCase = asignarRevisoresUseCase;
        this.listarAsignadasUseCase = listarAsignadasUseCase;
        this.registrarEvaluacionUseCase = registrarEvaluacionUseCase;
        this.registrarDecisionUseCase = registrarDecisionUseCase;
        this.consultarHistorialUseCase = consultarHistorialUseCase;
        this.repositoryPort = repositoryPort;
        this.preguntasClientPort = preguntasClientPort;
    }

    @PostMapping("/asignar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Asignar entre 1 y 3 revisores a una pregunta en revisión (HU4 - SSCRUM-56 a SSCRUM-61)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Asignación exitosa y evento publicado"),
        @ApiResponse(responseCode = "400", description = "Error en validación: 0 o más de 3 revisores, autor asignado, o revisor inexistente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo Administradores pueden asignar revisores"),
        @ApiResponse(responseCode = "409", description = "La pregunta no se encuentra en estado Pendiente de revisión")
    })
    public ResponseEntity<?> asignarRevisores(
            @RequestBody AsignarRevisoresRequestDTO request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        Long adminId = ResourceServerSecurityConfig.extractUserId(jwt);
        try {
            List<Revision> creadas = asignarRevisoresUseCase.execute(
                    request.getPreguntaId(), request.getRevisorIds(), adminId, correlationId
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
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Consultar listado de preguntas pendientes de revisión para el Administrador (HU4)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de preguntas pendientes sin asignar"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo ADMINISTRADOR")
    })
    public ResponseEntity<List<PreguntaDTO>> listarPendientes() {
        List<PreguntaDTO> pendientes = preguntasClientPort.listarPreguntasPendientes();
        List<PreguntaDTO> sinAsignar = pendientes.stream()
                .filter(p -> repositoryPort.findByPreguntaId(p.getId()).stream()
                        .noneMatch(r -> r.getEstado() == EstadoRevision.ASIGNADA || r.getEstado() == EstadoRevision.EVALUADA))
                .collect(Collectors.toList());
        return ResponseEntity.ok(sinAsignar);
    }

    @GetMapping("/asignadas")
    @PreAuthorize("hasRole('REVISOR')")
    @Operation(summary = "Listar preguntas asignadas al revisor autenticado con paginación y filtros (HU5 / HU12)",
               description = "Permite filtrar por estado de revisión, dificultad y competencia antes de paginar para totales exactos")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado paginado de revisiones"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: rol Revisor requerido")
    })
    public ResponseEntity<?> listarAsignadas(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "dificultad", required = false) String dificultad,
            @RequestParam(value = "competencia", required = false) String competencia) {
        Long revisorId = ResourceServerSecurityConfig.extractUserId(jwt);
        return ResponseEntity.ok(listarAsignadasUseCase.execute(revisorId, page, size, estado, dificultad, competencia));
    }

    @PostMapping("/{id}/evaluacion")
    @PreAuthorize("hasRole('REVISOR')")
    @Operation(summary = "Registrar evaluación y observaciones de una pregunta asignada (HU5 - SSCRUM-63)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evaluación registrada exitosamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "La pregunta no está asignada al revisor autenticado o rol inválido"),
        @ApiResponse(responseCode = "409", description = "La revisión ya fue finalizada")
    })
    public ResponseEntity<?> registrarEvaluacion(
            @PathVariable(value = "id") Long id,
            @RequestBody EvaluacionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        Long revisorId = ResourceServerSecurityConfig.extractUserId(jwt);
        try {
            Evaluacion eval = registrarEvaluacionUseCase.execute(id, request, revisorId);
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
    @PreAuthorize("hasRole('REVISOR')")
    @Operation(summary = "Registrar decisión final (Aprobar o Rechazar) con validación de observaciones y consenso (HU5 - SSCRUM-64 a SSCRUM-66)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Decisión registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Al rechazar debe existir al menos una observación no vacía"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "La revisión no pertenece al revisor autenticado o rol inválido"),
        @ApiResponse(responseCode = "409", description = "La revisión ya fue decidida previamente")
    })
    public ResponseEntity<?> registrarDecision(
            @PathVariable(value = "id") Long id,
            @RequestBody DecisionRequestDTO request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        Long revisorId = ResourceServerSecurityConfig.extractUserId(jwt);
        try {
            Revision rev = registrarDecisionUseCase.execute(id, request, revisorId, correlationId);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Decisión registrada exitosamente",
                    "estadoRevision", rev.getEstado().name()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pregunta/{id}/observaciones")
    @PreAuthorize("hasAnyRole('AUTOR', 'REVISOR', 'ADMINISTRADOR')")
    @Operation(summary = "Consultar observaciones visibles para el Autor de una pregunta rechazada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Observaciones obtenidas"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<List<Observacion>> listarObservaciones(@PathVariable(value = "id") Long id) {
        List<Observacion> visibles = repositoryPort.findObservacionesByPreguntaId(id).stream()
                .filter(Observacion::isVisibleAutor)
                .collect(Collectors.toList());
        return ResponseEntity.ok(visibles);
    }

    @GetMapping("/pregunta/{id}/revisores-emails")
    @PreAuthorize("hasAnyRole('AUTOR', 'ADMINISTRADOR')")
    @Operation(summary = "Consultar correos electrónicos de los revisores previos de una pregunta (HU6.S5.2)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Correos de revisores previos"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<List<String>> listarEmailsRevisoresPrevios(@PathVariable(value = "id") Long id) {
        List<String> emails = repositoryPort.findByPreguntaId(id).stream()
                .map(Revision::getRevisorEmail)
                .filter(e -> e != null && !e.isBlank())
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/pregunta/{id}/historial")
    @PreAuthorize("hasAnyRole('AUTOR', 'ADMINISTRADOR')")
    @Operation(summary = "Consultar historial de revisiones y observaciones por pregunta (HU6 - Subtareas 1 y 2)",
               description = "Agrupa revisiones por ronda con revisor, decisión, fecha, evaluaciones y observaciones. Solo el autor puede consultar.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo el autor de la pregunta puede consultar su historial"),
        @ApiResponse(responseCode = "404", description = "Pregunta no encontrada")
    })
    public ResponseEntity<?> consultarHistorial(
            @PathVariable(value = "id") Long id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long userId = ResourceServerSecurityConfig.extractUserId(jwt);
        try {
            Map<String, Object> historial = consultarHistorialUseCase.execute(id, userId, page, size);
            return ResponseEntity.ok(historial);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
