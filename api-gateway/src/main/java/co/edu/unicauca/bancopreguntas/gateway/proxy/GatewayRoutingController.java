package co.edu.unicauca.bancopreguntas.gateway.proxy;

import co.edu.unicauca.bancopreguntas.gateway.config.GatewayProperties;
import co.edu.unicauca.bancopreguntas.gateway.filter.CorrelationFilter;
import co.edu.unicauca.bancopreguntas.gateway.security.JwtValidator;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "API Gateway", description = "Enrutamiento centralizado, verificación de tokens Keycloak/JWT, roles, correlación y timeouts (HU7)")
public class GatewayRoutingController {

    private static final Logger log = LoggerFactory.getLogger(GatewayRoutingController.class);

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailers",
            "transfer-encoding",
            "upgrade",
            "content-length",
            "host"
    );

    private final GatewayProperties gatewayProperties;
    private final JwtValidator jwtValidator;
    private final RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Autowired
    public GatewayRoutingController(GatewayProperties gatewayProperties, JwtValidator jwtValidator) {
        this.gatewayProperties = gatewayProperties;
        this.jwtValidator = jwtValidator;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restTemplate = new RestTemplate(factory);
    }

    // Constructor para inyección de RestTemplate en pruebas
    public GatewayRoutingController(GatewayProperties gatewayProperties, JwtValidator jwtValidator, RestTemplate restTemplate) {
        this.gatewayProperties = gatewayProperties;
        this.jwtValidator = jwtValidator;
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/**")
    @Operation(summary = "Proxy inverso centralizado", description = "Enruta solicitudes a los microservicios con validación JWT contra Keycloak, propagación de identidad y correlación")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Petición procesada exitosamente por el microservicio"),
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado por rol no autorizado en la matriz de permisos"),
        @ApiResponse(responseCode = "404", description = "Ruta de servicio no encontrada"),
        @ApiResponse(responseCode = "503", description = "Servicio destino no disponible")
    })
    public ResponseEntity<?> routeRequest(
            HttpMethod method,
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {

        String path = request.getRequestURI();
        // Quitar prefijo /api/v1
        String subPath = path.startsWith("/api/v1") ? path.substring(7) : path;
        String query = request.getQueryString();

        String correlationId = (String) request.getAttribute(CorrelationFilter.CORRELATION_HEADER);
        if (correlationId == null) {
            correlationId = request.getHeader(CorrelationFilter.CORRELATION_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
        }

        // 1. Identificar servicio de destino
        String targetServiceBaseUrl = resolveTargetService(subPath);
        if (targetServiceBaseUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ruta no encontrada en API Gateway", "path", path));
        }

        // 2. Comprobar si requiere autenticación
        boolean isPublic = isPublicEndpoint(subPath, method);
        Claims claims = null;

        if (!isPublic) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token de autorización ausente o inválido"));
            }
            try {
                claims = jwtValidator.validateAndExtractClaims(authHeader);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token JWT expirado o inválido: " + e.getMessage()));
            }

            // 3. Control inicial de roles en el Gateway (HU7.S4)
            ResponseEntity<?> roleCheck = checkRoleAuthorization(subPath, method, claims);
            if (roleCheck != null) {
                return roleCheck;
            }
        }

        // 4. Preparar cabeceras para el microservicio de destino (HU7.S5.2: ignorar cabeceras x-user- no verificadas)
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String lowerName = name.toLowerCase();
                // Filtrar cabeceras Hop-by-Hop y cabeceras de identidad inyectadas por el cliente
                if (!HOP_BY_HOP_HEADERS.contains(lowerName) && !lowerName.startsWith("x-user-")) {
                    headers.addAll(name, Collections.list(request.getHeaders(name)));
                }
            }
        }

        // Inyectar o propagar cabeceras de contexto seguras extraídas de claims verificados
        headers.set(CorrelationFilter.CORRELATION_HEADER, correlationId);
        if (claims != null) {
            Long userId = jwtValidator.extractUserId(claims);
            String role = jwtValidator.extractRole(claims);
            String username = jwtValidator.extractUsername(claims);
            if (userId != null) headers.set("X-User-Id", String.valueOf(userId));
            if (role != null) headers.set("X-User-Role", role);
            if (username != null) headers.set("X-User-Name", username);
        }

        // 5. Construir URI de destino segura
        URI targetUri;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(targetServiceBaseUrl).path(subPath);
            if (query != null && !query.isBlank()) {
                builder.query(query);
            }
            targetUri = builder.build(true).toUri();
        } catch (Exception e) {
            targetUri = URI.create(targetServiceBaseUrl + subPath + (query != null && !query.isBlank() ? "?" + query : ""));
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    targetUri,
                    method,
                    entity,
                    byte[].class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((headerName, values) -> {
                if (!HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase())) {
                    responseHeaders.addAll(headerName, values);
                }
            });
            responseHeaders.set(CorrelationFilter.CORRELATION_HEADER, correlationId);

            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());

        } catch (HttpStatusCodeException ex) {
            HttpHeaders responseHeaders = new HttpHeaders();
            if (ex.getResponseHeaders() != null) {
                ex.getResponseHeaders().forEach((headerName, values) -> {
                    if (!HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase())) {
                        responseHeaders.addAll(headerName, values);
                    }
                });
            }
            responseHeaders.set(CorrelationFilter.CORRELATION_HEADER, correlationId);
            return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), responseHeaders, ex.getStatusCode());

        } catch (ResourceAccessException ex) {
            log.error("[Gateway] Timeout o falla de conexión hacia {}: {}", targetUri, ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header(CorrelationFilter.CORRELATION_HEADER, correlationId)
                    .body(Map.of(
                            "error", "Servicio destino no disponible o tiempo de espera agotado",
                            "servicioUrl", targetServiceBaseUrl,
                            "correlationId", correlationId
                    ));
        } catch (Exception ex) {
            log.error("[Gateway] Error inesperado enrutando a {}: {}", targetUri, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(CorrelationFilter.CORRELATION_HEADER, correlationId)
                    .body(Map.of(
                            "error", "Falla interna en el API Gateway",
                            "mensaje", ex.getMessage() != null ? ex.getMessage() : "Error no identificado",
                            "correlationId", correlationId
                    ));
        }
    }

    public String resolveTargetService(String subPath) {
        // HU2: Envío de pregunta a revisión pertenece a Lifecycle Service
        if (subPath.startsWith("/preguntas/") && subPath.contains("/enviar-revision")) {
            return gatewayProperties.getLifecycle();
        } else if (subPath.startsWith("/auth") || subPath.startsWith("/usuarios")) {
            return gatewayProperties.getUsers();
        } else if (subPath.startsWith("/preguntas") || subPath.startsWith("/catalogos")) {
            return gatewayProperties.getQuestions();
        } else if (subPath.startsWith("/validaciones")) {
            return gatewayProperties.getValidation();
        } else if (subPath.startsWith("/ciclo-vida")) {
            return gatewayProperties.getLifecycle();
        } else if (subPath.startsWith("/revisiones")) {
            return gatewayProperties.getReview();
        } else if (subPath.startsWith("/notificaciones")) {
            return gatewayProperties.getNotification();
        } else if (subPath.startsWith("/auditoria")) {
            return gatewayProperties.getAudit();
        }
        return null;
    }

    private boolean isPublicEndpoint(String subPath, HttpMethod method) {
        // Las solicitudes de preflight CORS (OPTIONS) deben permitirse siempre sin token
        if (method == HttpMethod.OPTIONS) {
            return true;
        }
        if (subPath.startsWith("/auth/login") || subPath.startsWith("/auth/token")) {
            return true;
        }
        if (subPath.startsWith("/catalogos") && method == HttpMethod.GET) {
            return true;
        }
        return false;
    }

    private ResponseEntity<?> checkRoleAuthorization(String subPath, HttpMethod method, Claims claims) {
        String role = jwtValidator.extractRole(claims);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Rol de usuario no identificado o sin permisos asignados"));
        }

        // HU4: Asignación de revisores y consulta de preguntas pendientes de asignación -> Solo ADMINISTRADOR
        if (subPath.startsWith("/revisiones/asignar") || 
            subPath.startsWith("/revisiones/pendientes") || 
            subPath.equals("/preguntas/pendientes") || 
            subPath.startsWith("/preguntas/pendientes?")) {
            if (!"ADMINISTRADOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: solo ADMINISTRADOR puede gestionar asignaciones"));
            }
        }

        // Registro de nuevos usuarios -> Solo ADMINISTRADOR
        if (subPath.startsWith("/usuarios") && method == HttpMethod.POST) {
            if (!"ADMINISTRADOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: solo ADMINISTRADOR puede registrar nuevos usuarios"));
            }
        }

        // HU5: Evaluaciones y decisiones de revisión -> Solo REVISOR
        if (subPath.startsWith("/revisiones/asignadas") || 
            (subPath.startsWith("/revisiones/") && (subPath.contains("/evaluacion") || subPath.contains("/decision")))) {
            if (!"REVISOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: solo REVISOR puede emitir evaluaciones o decisiones"));
            }
        }

        // HU6: Historial de revisiones -> Solo AUTOR o ADMINISTRADOR
        if (subPath.startsWith("/revisiones/pregunta/") && subPath.contains("/historial")) {
            if (!"AUTOR".equalsIgnoreCase(role) && !"ADMINISTRADOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: solo el AUTOR de la pregunta o ADMINISTRADOR pueden consultar el historial"));
            }
        }

        // Control para evitar auto-aprobación o alteración directa en Ciclo de Vida
        if (subPath.startsWith("/ciclo-vida") && (subPath.contains("/decision") || subPath.contains("/inicializar"))) {
            if (!"ADMINISTRADOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: endpoint de sincronización interna"));
            }
        }

        // Auditoría global -> Solo ADMINISTRADOR
        if (subPath.startsWith("/auditoria")) {
            if (!"ADMINISTRADOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: solo ADMINISTRADOR puede consultar auditoría global"));
            }
        }

        // Creación y edición de preguntas o envío a revisión -> Solo AUTOR (o Administrador)
        if (subPath.startsWith("/preguntas") && (method == HttpMethod.POST || method == HttpMethod.PUT)) {
            if (!"AUTOR".equalsIgnoreCase(role) && !"ADMINISTRADOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: rol insuficiente para crear o modificar preguntas"));
            }
        }

        return null; // Autorizado
    }
}
