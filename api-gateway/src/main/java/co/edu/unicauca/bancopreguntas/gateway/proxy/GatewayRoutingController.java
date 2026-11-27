package co.edu.unicauca.bancopreguntas.gateway.proxy;

import co.edu.unicauca.bancopreguntas.gateway.config.GatewayProperties;
import co.edu.unicauca.bancopreguntas.gateway.filter.CorrelationFilter;
import co.edu.unicauca.bancopreguntas.gateway.security.KeycloakJwtExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
@Tag(name = "API Gateway", description = "Enrutamiento centralizado con Spring Security Resource Server (Keycloak JWKS), roles, correlación y timeouts (HU7)")
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
    private final KeycloakJwtExtractor jwtExtractor;
    private final RestTemplate restTemplate;

    @Autowired
    public GatewayRoutingController(GatewayProperties gatewayProperties, KeycloakJwtExtractor jwtExtractor) {
        this.gatewayProperties = gatewayProperties;
        this.jwtExtractor = jwtExtractor;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restTemplate = new RestTemplate(factory);
    }

    // Constructor para inyección de RestTemplate en pruebas
    public GatewayRoutingController(GatewayProperties gatewayProperties,
                                    KeycloakJwtExtractor jwtExtractor,
                                    RestTemplate restTemplate) {
        this.gatewayProperties = gatewayProperties;
        this.jwtExtractor = jwtExtractor;
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/**")
    @Operation(summary = "Proxy inverso centralizado", description = "Enruta solicitudes a los microservicios propagando el JWT verificado de Keycloak, identidad verificada y correlación")
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

        // 2. Obtener JWT verificado por Spring Security Resource Server
        Jwt verifiedJwt = extractVerifiedJwt();

        // 3. Preparar cabeceras hacia el microservicio (HU7.S5.2: eliminar cabeceras X-User-* inyectadas por el cliente)
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String lowerName = name.toLowerCase();
                if (!HOP_BY_HOP_HEADERS.contains(lowerName) && !lowerName.startsWith("x-user-")) {
                    headers.addAll(name, Collections.list(request.getHeaders(name)));
                }
            }
        }

        // Propagar trazabilidad e identidad derivada exclusivamente del JWT verificado
        headers.set(CorrelationFilter.CORRELATION_HEADER, correlationId);
        if (verifiedJwt != null) {
            Long userId = jwtExtractor.extractUserId(verifiedJwt);
            String role = jwtExtractor.extractRole(verifiedJwt);
            String username = jwtExtractor.extractUsername(verifiedJwt);
            if (userId != null) headers.set("X-User-Id", String.valueOf(userId));
            if (role != null) headers.set("X-User-Role", role);
            if (username != null) headers.set("X-User-Name", username);
            // Asegurar propagación del Bearer Token verificado hacia el microservicio Resource Server
            if (!headers.containsKey(HttpHeaders.AUTHORIZATION) && verifiedJwt.getTokenValue() != null) {
                headers.setBearerAuth(verifiedJwt.getTokenValue());
            }
        }

        // 4. Construir URI de destino
        URI targetUri;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(targetServiceBaseUrl).path(subPath);
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

    private Jwt extractVerifiedJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }

    public String resolveTargetService(String subPath) {
        // HU2: Envío de pregunta a revisión pertenece a Lifecycle Service
        if (subPath.startsWith("/preguntas/") && subPath.contains("/enviar-revision")) {
            return gatewayProperties.getLifecycle();
        } else if (subPath.startsWith("/usuarios")) {
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
}
