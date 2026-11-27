package co.edu.unicauca.bancopreguntas.review;

import co.edu.unicauca.bancopreguntas.review.application.ports.output.PreguntasClientPort;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.RevisionRepositoryPort;
import co.edu.unicauca.bancopreguntas.review.application.usecases.*;
import co.edu.unicauca.bancopreguntas.review.controller.ReviewController;
import co.edu.unicauca.bancopreguntas.review.infrastructure.security.ResourceServerSecurityConfig;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de seguridad Resource Server + @PreAuthorize en review-service (HU7.S4.2, HU7.S5.2).
 * Verifica que las cabeceras X-User-* sin JWT son rechazadas (401) y que @PreAuthorize aplica roles (403).
 */
@WebMvcTest(controllers = ReviewController.class)
@Import({ResourceServerSecurityConfig.class, ReviewSecurityAuthorizationTest.TestJwtConfig.class})
class ReviewSecurityAuthorizationTest {

    private static KeyPair rsaKeyPair;

    @BeforeAll
    static void initKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        rsaKeyPair = kpg.generateKeyPair();
    }

    @TestConfiguration
    static class TestJwtConfig {
        @Bean
        @Primary
        JwtDecoder testJwtDecoder() {
            return NimbusJwtDecoder.withPublicKey((RSAPublicKey) rsaKeyPair.getPublic()).build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AsignarRevisoresUseCase asignarRevisoresUseCase;
    @MockitoBean private ListarAsignadasUseCase listarAsignadasUseCase;
    @MockitoBean private RegistrarEvaluacionUseCase registrarEvaluacionUseCase;
    @MockitoBean private RegistrarDecisionUseCase registrarDecisionUseCase;
    @MockitoBean private ConsultarHistorialUseCase consultarHistorialUseCase;
    @MockitoBean private RevisionRepositoryPort repositoryPort;
    @MockitoBean private PreguntasClientPort preguntasClientPort;

    private String token(String username, String role) throws Exception {
        long now = System.currentTimeMillis();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(username)
                .claim("preferred_username", username)
                .claim("realm_access", Map.of("roles", List.of(role)))
                .issueTime(new Date(now - 10_000L))
                .expirationTime(new Date(now + 3600_000L))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), claims);
        jwt.sign(new RSASSASigner(rsaKeyPair.getPrivate()));
        return jwt.serialize();
    }

    @Test
    @DisplayName("HU7.S5.2: Acceso directo con cabeceras X-User-* falsificadas y SIN token JWT responde 401")
    void accesoDirectoConCabecerasFalsificadasSinToken_responde401() throws Exception {
        mockMvc.perform(get("/revisiones/pendientes")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMINISTRADOR"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HU7.S4.2: @PreAuthorize bloquea con 403 cuando AUTOR intenta asignar revisores o ver asignadas")
    void preAuthorize_autorBloqueadoEnAsignarYAsignadas() throws Exception {
        String autorJwt = token("autor1", "AUTOR");

        mockMvc.perform(post("/revisiones/asignar")
                        .header("Authorization", "Bearer " + autorJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preguntaId\":1,\"revisorIds\":[4]}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/revisiones/asignadas")
                        .header("Authorization", "Bearer " + autorJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HU7.S4.2: @PreAuthorize permite ADMINISTRADOR en /pendientes y REVISOR en /asignadas")
    void preAuthorize_rolesAutorizadosPermitenAcceso() throws Exception {
        when(preguntasClientPort.listarPreguntasPendientes()).thenReturn(Collections.emptyList());
        when(listarAsignadasUseCase.execute(eq(4L), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(Map.of("content", Collections.emptyList(), "totalElements", 0L));

        mockMvc.perform(get("/revisiones/pendientes")
                        .header("Authorization", "Bearer " + token("admin", "ADMINISTRADOR")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/revisiones/asignadas")
                        .header("Authorization", "Bearer " + token("revisor1", "REVISOR")))
                .andExpect(status().isOk());
    }
}

