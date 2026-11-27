package co.edu.unicauca.bancopreguntas.questions.controller;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.questions.facade.QuestionQueryFacade;
import co.edu.unicauca.bancopreguntas.questions.security.ResourceServerSecurityConfig;
import co.edu.unicauca.bancopreguntas.questions.service.QuestionService;
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

import java.util.*;

@RestController
@RequestMapping("/preguntas")
@Tag(name = "Preguntas", description = "Operaciones CRUD, creación con Builder, listado paginado con Facade y versionado (HU1, HU3, HU6, HU7)")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionQueryFacade queryFacade;

    public QuestionController(QuestionService questionService, QuestionQueryFacade queryFacade) {
        this.questionService = questionService;
        this.queryFacade = queryFacade;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUTOR', 'ADMINISTRADOR')")
    @Operation(summary = "Crear nueva pregunta aplicando validación estructural y patrón Builder (HU1 - SSCRUM-34)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pregunta creada exitosamente en estado Borrador"),
        @ApiResponse(responseCode = "400", description = "Error de validación estructural con campos requeridos"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: rol AUTOR o ADMINISTRADOR requerido")
    })
    public ResponseEntity<?> crearPregunta(
            @RequestBody PreguntaDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = ResourceServerSecurityConfig.extractUserId(jwt);
        String userName = ResourceServerSecurityConfig.extractUsername(jwt);
        try {
            PreguntaDTO creada = questionService.crearPregunta(dto, userId, userName);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (QuestionService.ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "valido", false,
                    "errores", e.getErrores()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUTOR', 'ADMINISTRADOR')")
    @Operation(summary = "Listar preguntas con paginación, filtros combinados y fachada de ciclo de vida (HU3)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de preguntas obtenido exitosamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: rol insuficiente")
    })
    public ResponseEntity<?> listarPreguntas(
            @RequestParam(value = "estados", required = false) List<String> estados,
            @RequestParam(value = "estado", required = false) String estadoSingular,
            @RequestParam(value = "dificultad", required = false, defaultValue = "TODOS") String dificultad,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ResourceServerSecurityConfig.extractUserId(jwt);
        String userRole = ResourceServerSecurityConfig.extractRole(jwt);

        boolean esAdmin = "ADMINISTRADOR".equalsIgnoreCase(userRole);
        Long autorId = esAdmin ? null : userId;

        List<EstadoPreguntaDTO> estadosDTO = null;
        List<String> rawEstados = new ArrayList<>();
        if (estados != null) rawEstados.addAll(estados);
        if (estadoSingular != null && !estadoSingular.isBlank() && !"TODOS".equalsIgnoreCase(estadoSingular)) {
            rawEstados.add(estadoSingular);
        }
        if (!rawEstados.isEmpty()) {
            estadosDTO = new ArrayList<>();
            for (String est : rawEstados) {
                if (est != null && !est.isBlank()) {
                    for (String sub : est.split(",")) {
                        if (!sub.isBlank() && !"TODOS".equalsIgnoreCase(sub.trim())) {
                            estadosDTO.add(EstadoPreguntaDTO.fromString(sub.trim()));
                        }
                    }
                }
            }
            if (estadosDTO.isEmpty()) {
                estadosDTO = null;
            }
        }

        Map<String, Object> resultado = queryFacade.listarPreguntas(autorId, estadosDTO, dificultad, page, size);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTOR', 'REVISOR', 'ADMINISTRADOR')")
    @Operation(summary = "Consultar el detalle completo de una pregunta con acciones permitidas (HU3)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle de la pregunta obtenido exitosamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para ver esta pregunta"),
        @ApiResponse(responseCode = "404", description = "Pregunta no encontrada")
    })
    public ResponseEntity<?> consultarDetalle(
            @PathVariable(value = "id") Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = ResourceServerSecurityConfig.extractUserId(jwt);
        String userRole = ResourceServerSecurityConfig.extractRole(jwt);
        try {
            boolean puedeVerTodas = "ADMINISTRADOR".equalsIgnoreCase(userRole) || "REVISOR".equalsIgnoreCase(userRole);
            return queryFacade.consultarDetalle(id, userId, puedeVerTodas)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTOR', 'ADMINISTRADOR')")
    @Operation(summary = "Editar una pregunta en estado Borrador (HU3 / SSCRUM-46)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pregunta actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación en campos"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para editar esta pregunta"),
        @ApiResponse(responseCode = "409", description = "No se puede editar una pregunta fuera de Borrador")
    })
    public ResponseEntity<?> editarBorrador(
            @PathVariable(value = "id") Long id,
            @RequestBody PreguntaDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = ResourceServerSecurityConfig.extractUserId(jwt);
        try {
            PreguntaDTO actualizada = questionService.editarBorrador(id, dto, userId);
            return ResponseEntity.ok(actualizada);
        } catch (QuestionService.ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (QuestionService.ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("valido", false, "errores", e.getErrores()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/nueva-version")
    @PreAuthorize("hasAnyRole('AUTOR', 'ADMINISTRADOR')")
    @Operation(summary = "Crear nueva versión mejorada a partir de una pregunta rechazada (HU6.S3 / HU6.S4)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nueva versión creada exitosamente en estado Borrador"),
        @ApiResponse(responseCode = "400", description = "Error de validación estructural"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para versionar esta pregunta"),
        @ApiResponse(responseCode = "409", description = "La pregunta no está en estado Rechazada")
    })
    public ResponseEntity<?> crearNuevaVersion(
            @PathVariable(value = "id") Long id,
            @RequestBody(required = false) PreguntaDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = ResourceServerSecurityConfig.extractUserId(jwt);
        try {
            PreguntaDTO versionada = questionService.crearNuevaVersion(id, dto, userId);
            return ResponseEntity.ok(versionada);
        } catch (QuestionService.ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (QuestionService.ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("valido", false, "errores", e.getErrores()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar preguntas en estado Pendiente de revisión para asignación por el Administrador (HU4)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de preguntas pendientes"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: solo ADMINISTRADOR")
    })
    public ResponseEntity<List<PreguntaDTO>> listarPendientes() {
        return ResponseEntity.ok(questionService.listarPendientesDeRevision());
    }

    @PutMapping("/{id}/proyeccion-estado")
    @PreAuthorize("hasAnyRole('AUTOR', 'REVISOR', 'ADMINISTRADOR')")
    @Operation(summary = "Sincronizar proyección local de estado desde eventos de ciclo de vida")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Proyección de estado sincronizada"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<?> actualizarProyeccion(
            @PathVariable(value = "id") Long id,
            @RequestParam(value = "estado") String estado) {
        questionService.actualizarProyeccionEstado(id, EstadoPreguntaDTO.fromString(estado));
        return ResponseEntity.ok(Map.of("preguntaId", id, "estado", estado));
    }
}
