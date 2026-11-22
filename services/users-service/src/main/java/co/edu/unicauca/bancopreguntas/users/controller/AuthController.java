package co.edu.unicauca.bancopreguntas.users.controller;

import co.edu.unicauca.bancopreguntas.contracts.dto.LoginRequestDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.TokenResponseDTO;
import co.edu.unicauca.bancopreguntas.users.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @deprecated En Sprint 3 (HU7), la emisión oficial de tokens JWT se traslada a Keycloak como IdP autoritativo.
 * Este endpoint se mantiene exclusivamente como fallback de compatibilidad para entornos de prueba offline.
 */
@Deprecated(since = "2.0.0")
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación (Legacy)", description = "Endpoints de login legacy. En Sprint 3 Keycloak es la fuente de verdad (HU7)")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT (Legacy - Usar Keycloak en producción)", deprecated = true)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "403", description = "Usuario inactivo")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            TokenResponseDTO response = usuarioService.autenticar(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }
}
