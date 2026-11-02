package co.edu.unicauca.bancopreguntas.gateway;

import co.edu.unicauca.bancopreguntas.gateway.config.GatewayProperties;
import co.edu.unicauca.bancopreguntas.gateway.filter.CorrelationFilter;
import co.edu.unicauca.bancopreguntas.gateway.proxy.GatewayRoutingController;
import co.edu.unicauca.bancopreguntas.gateway.security.JwtValidator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GatewaySecurityAndRoutingTest {

    private static final String SECRET = "SaberProSecretKey2026SuperSecureMustBeAtLeast256BitsLongForHMACSHA256";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private GatewayProperties properties;
    private JwtValidator jwtValidator;
    private GatewayRoutingController controller;

    private String createTestToken(Long userId, String username, String role) {
        return Jwts.builder()
                .claims(Map.of("userId", userId, "username", username, "role", role))
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @BeforeEach
    void setUp() {
        properties = new GatewayProperties();
        jwtValidator = new JwtValidator(SECRET);

        // Stub RestTemplate que simula respuestas de los microservicios
        RestTemplate stubRestTemplate = new RestTemplate() {
            @Override
            public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
                    Class<T> responseType) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set(CorrelationFilter.CORRELATION_HEADER,
                        requestEntity.getHeaders().getFirst(CorrelationFilter.CORRELATION_HEADER));
                byte[] body = "{\"status\":\"ok\",\"mocked\":true}".getBytes(StandardCharsets.UTF_8);
                return (ResponseEntity<T>) new ResponseEntity<>(body, headers, HttpStatus.OK);
            }
        };

        controller = new GatewayRoutingController(properties, jwtValidator, stubRestTemplate);
    }

    @Test
    @DisplayName("Público: /api/v1/auth/login debe permitir acceso sin token")
    void testPublicEndpointAuthLogin() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setAttribute(CorrelationFilter.CORRELATION_HEADER, "corr-test-1");

        ResponseEntity<?> response = controller.routeRequest(HttpMethod.POST, request, "{}".getBytes());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(CorrelationFilter.CORRELATION_HEADER));
    }

    @Test
    @DisplayName("CORS Preflight: Solicitudes OPTIONS deben permitirse sin token de autorización")
    void testOptionsMethodIsPublic() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/preguntas");
        request.setAttribute(CorrelationFilter.CORRELATION_HEADER, "corr-cors-1");

        ResponseEntity<?> response = controller.routeRequest(HttpMethod.OPTIONS, request, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(CorrelationFilter.CORRELATION_HEADER));
    }

    @Test
    @DisplayName("Seguridad: endpoints protegidos sin token deben responder 401 Unauthorized")
    void testProtectedWithoutToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/preguntas");
        request.setAttribute(CorrelationFilter.CORRELATION_HEADER, "corr-test-2");

        ResponseEntity<?> response = controller.routeRequest(HttpMethod.GET, request, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Seguridad: token inválido o corrupto debe responder 401 Unauthorized")
    void testProtectedWithInvalidToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/preguntas");
        request.addHeader("Authorization", "Bearer tokenInvalido12345");
        request.setAttribute(CorrelationFilter.CORRELATION_HEADER, "corr-test-3");

        ResponseEntity<?> response = controller.routeRequest(HttpMethod.GET, request, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Enrutamiento HU2: /preguntas/{id}/enviar-revision debe enrutar a lifecycle-service")
    void testRouteEnviarARevisionDirectsToLifecycle() {
        String target = controller.resolveTargetService("/preguntas/42/enviar-revision");
        assertEquals(properties.getLifecycle(), target, "El endpoint enviar-revision debe ser dirigido a lifecycle-service");

    }

    @Test
    @DisplayName("Control de Roles: Autor intentando asignar revisores responde 403 Forbidden")
    void testRoleForbiddenAutorAssigningReviewers() {
        String tokenAutor = createTestToken(2L, "juan_autor", "AUTOR");

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/revisiones/asignar");
        request.addHeader("Authorization", "Bearer " + tokenAutor);
        request.setAttribute(CorrelationFilter.CORRELATION_HEADER, "corr-test-4");

        ResponseEntity<?> response = controller.routeRequest(HttpMethod.POST, request, "{}".getBytes());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Control de Roles: Administrador asignando revisores responde 200 OK y propaga correlación")
    void testRoleAllowedAdminAssigningReviewers() {
        String tokenAdmin = createTestToken(1L, "admin_user", "ADMINISTRADOR");

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/revisiones/asignar");
        request.addHeader("Authorization", "Bearer " + tokenAdmin);
        request.setAttribute(CorrelationFilter.CORRELATION_HEADER, "corr-admin-99");

        ResponseEntity<?> response = controller.routeRequest(HttpMethod.POST, request, "{}".getBytes());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("corr-admin-99", response.getHeaders().getFirst(CorrelationFilter.CORRELATION_HEADER));
    }

    @Test
    @DisplayName("Control de Roles: Revisor consultando revisiones asignadas responde 200 OK")
    void testRoleAllowedRevisor() {
        String tokenRevisor = createTestToken(4L, "revisor_user", "REVISOR");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/revisiones/asignadas");
        request.addHeader("Authorization", "Bearer " + tokenRevisor);
        request.setAttribute(CorrelationFilter.CORRELATION_HEADER, "corr-rev-1");

        ResponseEntity<?> response = controller.routeRequest(HttpMethod.GET, request, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Control de Roles: Autor intentando consultar /preguntas/pendientes responde 403 Forbidden")
    void testRoleForbiddenAutorConsultingPendientes() {
        String tokenAutor = createTestToken(2L, "juan_autor", "AUTOR");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/preguntas/pendientes");
        request.addHeader("Authorization", "Bearer " + tokenAutor);
        request.setAttribute(CorrelationFilter.CORRELATION_HEADER, "corr-pend-1");

        ResponseEntity<?> response = controller.routeRequest(HttpMethod.GET, request, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Control de Roles: Autor intentando crear usuario (POST /usuarios) responde 403 Forbidden")
    void testRoleForbiddenAutorCreatingUser() {
        String tokenAutor = createTestToken(2L, "juan_autor", "AUTOR");

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/usuarios");
        request.addHeader("Authorization", "Bearer " + tokenAutor);
        request.setAttribute(CorrelationFilter.CORRELATION_HEADER, "corr-usr-1");

        ResponseEntity<?> response = controller.routeRequest(HttpMethod.POST, request, "{}".getBytes());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
