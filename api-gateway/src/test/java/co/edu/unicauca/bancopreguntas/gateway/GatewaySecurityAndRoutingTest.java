package co.edu.unicauca.bancopreguntas.gateway;

import co.edu.unicauca.bancopreguntas.gateway.config.GatewayProperties;
import co.edu.unicauca.bancopreguntas.gateway.filter.CorrelationFilter;
import co.edu.unicauca.bancopreguntas.gateway.proxy.GatewayRoutingController;
import co.edu.unicauca.bancopreguntas.gateway.security.GatewaySecurityConfig;
import co.edu.unicauca.bancopreguntas.gateway.security.KeycloakJwtExtractor;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas automatizadas del API Gateway con Spring Security OAuth2 Resource Server (HU7.S3.4, HU7.S4.4, HU7.S5.2).
 * Valida tokens RS256 válidos, expirados y firmados con otra clave, matriz de roles 403 y anti-suplantación X-User-*.
 */
@WebMvcTest(useDefaultFilters = false)
@Import({
        GatewaySecurityConfig.class,
        KeycloakJwtExtractor.class,
        GatewayProperties.class,
        CorrelationFilter.class,
        GatewaySecurityAndRoutingTest.TestGatewayBeansConfig.class
})
class GatewaySecurityAndRoutingTest {

    private static KeyPair validRsaKeyPair;
    private static KeyPair otherRsaKeyPair;
    private static final AtomicReference<HttpHeaders> forwardedHeaders = new AtomicReference<>();

    @BeforeAll
    static void initKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        validRsaKeyPair = kpg.generateKeyPair();
        otherRsaKeyPair = kpg.generateKeyPair();
    }

    @TestConfiguration
    static class TestGatewayBeansConfig {
        @Bean
        @Primary
        JwtDecoder testJwtDecoder() {
            return NimbusJwtDecoder.withPublicKey((RSAPublicKey) validRsaKeyPair.getPublic()).build();
        }

        @Bean
        @Primary
        GatewayRoutingController testGatewayRoutingController(GatewayProperties props, KeycloakJwtExtractor extractor) {
            RestTemplate stubRestTemplate = new RestTemplate() {
                @Override
                @SuppressWarnings("unchecked")
                public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
                                                      Class<T> responseType) {
                    forwardedHeaders.set(requestEntity.getHeaders());
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    String corr = requestEntity.getHeaders().getFirst(CorrelationFilter.CORRELATION_HEADER);
                    if (corr != null) {
                        headers.set(CorrelationFilter.CORRELATION_HEADER, corr);
                    }
                    byte[] body = "{\"status\":\"ok\",\"mocked\":true}".getBytes(StandardCharsets.UTF_8);
                    return (ResponseEntity<T>) new ResponseEntity<>(body, headers, HttpStatus.OK);
                }
            };
            return new GatewayRoutingController(props, extractor, stubRestTemplate);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GatewayRoutingController controller;

    @Autowired
    private GatewayProperties properties;

    @BeforeEach
    void resetCapturedHeaders() {
        forwardedHeaders.set(null);
    }

    private String createKeycloakRs256Token(String username, String role, boolean expired, KeyPair signingKeyPair) throws Exception {
        long now = System.currentTimeMillis();
        Date exp = new Date(expired ? now - 3600_000L : now + 3600_000L);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(username)
                .claim("preferred_username", username)
                .claim("realm_access", Map.of("roles", List.of(role, "default-roles-saberpro")))
                .issueTime(new Date(now - 60_000L))
                .expirationTime(exp)
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("kc-key-1").build(),
                claims
        );
        signedJWT.sign(new RSASSASigner(signingKeyPair.getPrivate()));
        return signedJWT.serialize();
    }

    @Test
    @DisplayName("Público: GET /api/v1/catalogos/competencias debe permitir acceso sin token")
    void testPublicCatalogosEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/catalogos/competencias"))
                .andExpect(status().isOk())
                .andExpect(header().exists(CorrelationFilter.CORRELATION_HEADER));
    }

    @Test
    @DisplayName("CORS Preflight: Solicitudes OPTIONS deben permitirse sin token")
    void testOptionsMethodIsPublic() throws Exception {
        mockMvc.perform(options("/api/v1/preguntas"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HU7.S3: Endpoint protegido sin token responde 401 Unauthorized")
    void testProtectedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/preguntas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HU7.S3: Token malformado o corrupto responde 401 Unauthorized")
    void testProtectedWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/preguntas")
                        .header("Authorization", "Bearer tokenInvalido12345"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HU7.S3.4: Token Keycloak RS256 expirado responde 401 Unauthorized")
    void testKeycloakRsaTokenExpirado_responde401() throws Exception {
        String expiredToken = createKeycloakRs256Token("autor1", "AUTOR", true, validRsaKeyPair);

        mockMvc.perform(get("/api/v1/preguntas/1")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HU7.S3.4: Token Keycloak RS256 firmado con otra clave RSA responde 401 Unauthorized")
    void testKeycloakRsaTokenOtraClave_responde401() throws Exception {
        String wrongKeyToken = createKeycloakRs256Token("autor1", "AUTOR", false, otherRsaKeyPair);

        mockMvc.perform(get("/api/v1/preguntas/1")
                        .header("Authorization", "Bearer " + wrongKeyToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HU7.S3.4: Token Keycloak RS256 con payload alterado responde 401 Unauthorized")
    void testKeycloakRsaTokenFirmaAlterada_responde401() throws Exception {
        String token = createKeycloakRs256Token("autor1", "AUTOR", false, validRsaKeyPair);
        String[] parts = token.split("\\.");
        String alteredToken = parts[0] + "." + parts[1] + "X." + parts[2];

        mockMvc.perform(get("/api/v1/preguntas/1")
                        .header("Authorization", "Bearer " + alteredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HU7.S3.2: Token Keycloak RS256 válido propaga identidad, rol y Bearer al microservicio")
    void testKeycloakRsaTokenValido_propagaHeaders() throws Exception {
        String token = createKeycloakRs256Token("autor1", "AUTOR", false, validRsaKeyPair);

        mockMvc.perform(get("/api/v1/preguntas/1")
                        .header("Authorization", "Bearer " + token)
                        .header(CorrelationFilter.CORRELATION_HEADER, "corr-rsa-1"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationFilter.CORRELATION_HEADER, "corr-rsa-1"));

        assertNotNull(forwardedHeaders.get());
        assertEquals("AUTOR", forwardedHeaders.get().getFirst("X-User-Role"));
        assertEquals("autor1", forwardedHeaders.get().getFirst("X-User-Name"));
        assertEquals("2", forwardedHeaders.get().getFirst("X-User-Id"));
        assertEquals("Bearer " + token, forwardedHeaders.get().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    @DisplayName("HU7.S5.2: Cabeceras X-User-* inyectadas por el cliente son ignoradas y sobrescritas con el JWT")
    void testSpoofedHeadersAreIgnoredAndOverwritten() throws Exception {
        String token = createKeycloakRs256Token("autor1", "AUTOR", false, validRsaKeyPair);

        mockMvc.perform(get("/api/v1/preguntas/1")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", "999")
                        .header("X-User-Role", "ADMINISTRADOR"))
                .andExpect(status().isOk());

        assertNotNull(forwardedHeaders.get());
        assertEquals("2", forwardedHeaders.get().getFirst("X-User-Id"));
        assertEquals("AUTOR", forwardedHeaders.get().getFirst("X-User-Role"));
    }

    @Test
    @DisplayName("HU2 Enrutamiento: /preguntas/{id}/enviar-revision enruta a lifecycle-service")
    void testRouteEnviarARevisionDirectsToLifecycle() {
        assertEquals(properties.getLifecycle(), controller.resolveTargetService("/preguntas/42/enviar-revision"));
    }

    @Test
    @DisplayName("HU7.S4.4 - AUTOR: permitido en crear/listar/historial y 403 en asignar/pendientes/usuarios/auditoría/asignadas")
    void testAutorRoleMatrix() throws Exception {
        String tokenAutor = createKeycloakRs256Token("autor1", "AUTOR", false, validRsaKeyPair);

        mockMvc.perform(post("/api/v1/preguntas").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/revisiones/pregunta/10/historial")
                        .header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/revisiones/asignar").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/preguntas/pendientes")
                        .header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/auditoria")
                        .header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/revisiones/asignadas")
                        .header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HU7.S4.4 - REVISOR: permitido en asignadas/evaluacion/decision y 403 en asignar/historial/crear pregunta")
    void testRevisorRoleMatrix() throws Exception {
        String tokenRevisor = createKeycloakRs256Token("revisor1", "REVISOR", false, validRsaKeyPair);

        mockMvc.perform(get("/api/v1/revisiones/asignadas")
                        .header("Authorization", "Bearer " + tokenRevisor))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/revisiones/5/evaluacion").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Authorization", "Bearer " + tokenRevisor))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/revisiones/5/decision").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Authorization", "Bearer " + tokenRevisor))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/revisiones/asignar").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Authorization", "Bearer " + tokenRevisor))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/revisiones/pregunta/10/historial")
                        .header("Authorization", "Bearer " + tokenRevisor))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/preguntas").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Authorization", "Bearer " + tokenRevisor))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HU7.S4.4 - ADMINISTRADOR: permitido en asignar/pendientes/auditoría/usuarios y 403 en decidir revisión")
    void testAdminRoleMatrix() throws Exception {
        String tokenAdmin = createKeycloakRs256Token("admin", "ADMINISTRADOR", false, validRsaKeyPair);

        mockMvc.perform(post("/api/v1/revisiones/asignar").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .header(CorrelationFilter.CORRELATION_HEADER, "corr-admin-99"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationFilter.CORRELATION_HEADER, "corr-admin-99"));
        mockMvc.perform(get("/api/v1/revisiones/pendientes")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/auditoria")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/revisiones/5/decision").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HU7.S2.3: Endpoint legacy /api/v1/auth/login ya no es público ni enrutado")
    void testLegacyAuthLoginRemoved() throws Exception {
        assertNull(controller.resolveTargetService("/auth/login"));
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
