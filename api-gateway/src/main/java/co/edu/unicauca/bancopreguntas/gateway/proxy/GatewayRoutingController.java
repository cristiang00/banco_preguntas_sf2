package co.edu.unicauca.bancopreguntas.gateway.proxy;

import co.edu.unicauca.bancopreguntas.gateway.config.GatewayProperties;
import co.edu.unicauca.bancopreguntas.gateway.filter.CorrelationFilter;
import co.edu.unicauca.bancopreguntas.gateway.security.JwtValidator;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "API Gateway", description = "Enrutamiento centralizado, verificación de tokens, roles, correlación y timeouts")
public class GatewayRoutingController {

    private static final Logger log = LoggerFactory.getLogger(GatewayRoutingController.class);

    private final GatewayProperties gatewayProperties;
    private final JwtValidator jwtValidator;
    private final RestTemplate restTemplate;

    public GatewayRoutingController(GatewayProperties gatewayProperties, JwtValidator jwtValidator) {
        this.gatewayProperties = gatewayProperties;
        this.jwtValidator = jwtValidator;
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    // Constructor para inyección de RestTemplate en pruebas
    public GatewayRoutingController(GatewayProperties gatewayProperties, JwtValidator jwtValidator, RestTemplate restTemplate) {
        this.gatewayProperties = gatewayProperties;
        this.jwtValidator = jwtValidator;
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/**")
    @Operation(summary = "Proxy inverso centralizado", description = "Enruta solicitudes a los microservicios con validación JWT, propagación de identidad y correlación")
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

            // 3. Control inicial de roles en el Gateway
            ResponseEntity<?> roleCheck = checkRoleAuthorization(subPath, method, claims);
            if (roleCheck != null) {
                return roleCheck;
            }
        }

        // 4. Preparar cabeceras para el microservicio de destino
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (!name.equalsIgnoreCase("content-length") && !name.equalsIgnoreCase("host")) {
                    headers.addAll(name, Collections.list(request.getHeaders(name)));
                }
            }
        }

        // Inyectar o propagar cabeceras de contexto
        headers.set(CorrelationFilter.CORRELATION_HEADER, correlationId);
        if (claims != null) {
            Object userId = claims.get("userId");
            Object role = claims.get("role");
            String username = claims.getSubject();
            if (userId != null) headers.set("X-User-Id", String.valueOf(userId));
            if (role != null) headers.set("X-User-Role", String.valueOf(role));
            if (username != null) headers.set("X-User-Name", username);
        }

        // 5. Construir URI de destino
        String targetUrl = targetServiceBaseUrl + subPath + (query != null ? "?" + query : "");
        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    URI.create(targetUrl),
                    method,
                    entity,
                    byte[].class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach(responseHeaders::addAll);
            responseHeaders.set(CorrelationFilter.CORRELATION_HEADER, correlationId);

            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());

        } catch (HttpStatusCodeException ex) {
            HttpHeaders responseHeaders = new HttpHeaders();
            ex.getResponseHeaders().forEach(responseHeaders::addAll);
            responseHeaders.set(CorrelationFilter.CORRELATION_HEADER, correlationId);
            return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), responseHeaders, ex.getStatusCode());

        } catch (ResourceAccessException ex) {
            log.error("[Gateway] Timeout o falla de conexión hacia {}: {}", targetUrl, ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header(CorrelationFilter.CORRELATION_HEADER, correlationId)
                    .body(Map.of(
                            "error", "Servicio destino no disponible o tiempo de espera agotado",
                            "servicioUrl", targetServiceBaseUrl,
                            "correlationId", correlationId
                    ));
        } catch (Exception ex) {
            log.error("[Gateway] Error inesperado enrutando a {}: {}", targetUrl, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(CorrelationFilter.CORRELATION_HEADER, correlationId)
                    .body(Map.of(
                            "error", "Falla interna en el API Gateway",
                            "mensaje", ex.getMessage(),
                            "correlationId", correlationId
                    ));
        }
    }

    private String resolveTargetService(String subPath) {
        if (subPath.startsWith("/auth") || subPath.startsWith("/usuarios")) {
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
        if (subPath.startsWith("/auth/login") || subPath.startsWith("/auth/token")) {
            return true;
        }
        if (subPath.startsWith("/catalogos") && method == HttpMethod.GET) {
            return true;
        }
        return false;
    }

    private ResponseEntity<?> checkRoleAuthorization(String subPath, HttpMethod method, Claims claims) {
        String role = (String) claims.get("role");
        if (role == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Rol de usuario no identificado"));
        }

        // HU4: Asignación de revisores y consulta de preguntas pendientes de asignación -> Solo ADMINISTRADOR
        if (subPath.startsWith("/revisiones/asignar") || subPath.startsWith("/revisiones/pendientes")) {
            if (!"ADMINISTRADOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: solo ADMINISTRADOR puede gestionar asignaciones"));
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

        // Auditoría global -> Solo ADMINISTRADOR
        if (subPath.startsWith("/auditoria")) {
            if (!"ADMINISTRADOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: solo ADMINISTRADOR puede consultar auditoría global"));
            }
        }

        // Creación y edición de preguntas -> Solo AUTOR (o Administrador en caso especial)
        if (subPath.startsWith("/preguntas") && (method == HttpMethod.POST || method == HttpMethod.PUT)) {
            if (!"AUTOR".equalsIgnoreCase(role) && !"ADMINISTRADOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: rol insuficiente para crear o modificar preguntas"));
            }
        }

        return null; // Autorizado
    }
}
