package co.edu.unicauca.bancopreguntas.questions.controller;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.questions.facade.QuestionQueryFacade;
import co.edu.unicauca.bancopreguntas.questions.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/preguntas")
@Tag(name = "Preguntas", description = "Operaciones CRUD, creación con Builder, listado paginado con Facade y filtros")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionQueryFacade queryFacade;

    public QuestionController(QuestionService questionService, QuestionQueryFacade queryFacade) {
        this.questionService = questionService;
        this.queryFacade = queryFacade;
    }

    @PostMapping
    @Operation(summary = "Crear nueva pregunta aplicando validación estructural y patrón Builder (HU1 - SSCRUM-34)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pregunta creada exitosamente en estado Borrador"),
        @ApiResponse(responseCode = "400", description = "Error de validación estructural con campos requeridos")
    })
    public ResponseEntity<?> crearPregunta(
            @RequestBody PreguntaDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "3") Long userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "Autor") String userName) {
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
    @Operation(summary = "Listar preguntas con paginación, filtros combinados y fachada de ciclo de vida (HU3)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de preguntas obtenido exitosamente")
    })
    public ResponseEntity<Map<String, Object>> listarPreguntas(
            @RequestParam(value = "estados", required = false) List<String> estados,
            @RequestParam(value = "dificultad", required = false, defaultValue = "TODOS") String dificultad,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Id", defaultValue = "3") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "AUTOR") String userRole) {

        boolean esAdmin = "ADMINISTRADOR".equalsIgnoreCase(userRole);
        Long autorId = esAdmin ? null : userId;

        List<EstadoPreguntaDTO> estadosDTO = null;
        if (estados != null && !estados.isEmpty()) {
            estadosDTO = new ArrayList<>();
            for (String est : estados) {
                if (est != null && !est.isBlank()) {
                    for (String sub : est.split(",")) {
                        if (!sub.isBlank()) {
                            estadosDTO.add(EstadoPreguntaDTO.fromString(sub.trim()));
                        }
                    }
                }
            }
        }

        Map<String, Object> resultado = queryFacade.listarPreguntas(autorId, estadosDTO, dificultad, page, size);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar el detalle completo de una pregunta con acciones permitidas (HU3)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle de la pregunta obtenido exitosamente"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para ver esta pregunta"),
        @ApiResponse(responseCode = "404", description = "Pregunta no encontrada")
    })
    public ResponseEntity<?> consultarDetalle(
            @PathVariable(value = "id") Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "3") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "AUTOR") String userRole) {
        try {
            boolean esAdmin = "ADMINISTRADOR".equalsIgnoreCase(userRole);
            return queryFacade.consultarDetalle(id, userId, esAdmin)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar una pregunta en estado Borrador (HU3 / SSCRUM-46)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pregunta actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación en campos"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para editar esta pregunta"),
        @ApiResponse(responseCode = "409", description = "No se puede editar una pregunta fuera de Borrador")
    })
    public ResponseEntity<?> editarBorrador(
            @PathVariable(value = "id") Long id,
            @RequestBody PreguntaDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "3") Long userId) {
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

    @GetMapping("/pendientes")
    @Operation(summary = "Listar preguntas en estado Pendiente de revisión para asignación por el Administrador (HU4)")
    public ResponseEntity<List<PreguntaDTO>> listarPendientes() {
        return ResponseEntity.ok(questionService.listarPendientesDeRevision());
    }

    @PutMapping("/{id}/proyeccion-estado")
    @Operation(summary = "Sincronizar proyección local de estado desde eventos de ciclo de vida")
    public ResponseEntity<?> actualizarProyeccion(
            @PathVariable(value = "id") Long id,
            @RequestParam(value = "estado") String estado) {
        questionService.actualizarProyeccionEstado(id, EstadoPreguntaDTO.fromString(estado));
        return ResponseEntity.ok(Map.of("preguntaId", id, "estado", estado));
    }
}

