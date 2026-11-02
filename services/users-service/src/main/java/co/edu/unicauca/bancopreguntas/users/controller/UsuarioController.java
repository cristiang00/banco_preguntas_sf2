package co.edu.unicauca.bancopreguntas.users.controller;

import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.users.domain.Rol;
import co.edu.unicauca.bancopreguntas.users.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Operaciones de consulta y administración de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/revisores")
    @Operation(summary = "Consultar lista de revisores activos y elegibles (HU4 - SSCRUM-60)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de revisores disponibles")
    })
    public ResponseEntity<List<UserSummaryDTO>> listarRevisores() {
        return ResponseEntity.ok(usuarioService.listarRevisores());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar usuario por su identificador único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios del sistema")
    public ResponseEntity<List<UserSummaryDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo usuario")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String fullName = body.get("fullName");
            String email = body.get("email");
            String password = body.get("password");
            String rolStr = body.get("rol");
            Rol rol = Rol.valueOf(rolStr.toUpperCase());
            UserSummaryDTO created = usuarioService.registrar(username, fullName, email, password, rol);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

