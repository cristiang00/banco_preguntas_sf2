package co.edu.unicauca.bancopreguntas.gateway.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;

/**
 * Configuración Spring Security OAuth2 Resource Server para el API Gateway (HU7.S3 / HU7.S4).
 * Valida firma RS256 y expiración contra el endpoint JWKS de Keycloak y aplica matriz rol-endpoint.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class GatewaySecurityConfig {

    private final KeycloakJwtExtractor jwtExtractor;

    public GatewaySecurityConfig(KeycloakJwtExtractor jwtExtractor) {
        this.jwtExtractor = jwtExtractor;
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${keycloak.jwks-url:http://localhost:8089/realms/saberpro/protocol/openid-connect/certs}") String jwksUrl) {
        return NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtExtractor::extractAuthorities);
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthConverter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Preflight CORS, healthcheck, Swagger y catálogos públicos
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/catalogos/**").permitAll()

                // HU4 / HU7.S4: Asignación de revisores y pendientes -> Solo ADMINISTRADOR
                .requestMatchers("/api/v1/revisiones/asignar", "/api/v1/revisiones/pendientes", "/api/v1/preguntas/pendientes").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/v1/auditoria/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/v1/ciclo-vida/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/v1/notificaciones/eventos").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/v1/revisiones/pregunta/*/revisores-emails").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/v1/preguntas/*/proyeccion-estado").hasRole("ADMINISTRADOR")

                // HU5 / HU7.S4: Revisión, evaluación y decisión -> Solo REVISOR
                .requestMatchers("/api/v1/revisiones/asignadas").hasRole("REVISOR")
                .requestMatchers(HttpMethod.POST, "/api/v1/revisiones/*/evaluacion").hasRole("REVISOR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/revisiones/*/decision").hasRole("REVISOR")

                // HU6 / HU7.S4: Historial y observaciones -> AUTOR o ADMINISTRADOR
                .requestMatchers(HttpMethod.GET, "/api/v1/revisiones/pregunta/*/historial").hasAnyRole("AUTOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/v1/revisiones/pregunta/*/observaciones").hasAnyRole("AUTOR", "ADMINISTRADOR")

                // HU1, HU2, HU3, HU6 / HU7.S4: Gestión de preguntas -> AUTOR (o ADMINISTRADOR)
                .requestMatchers(HttpMethod.POST, "/api/v1/preguntas/**").hasAnyRole("AUTOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/preguntas/**").hasAnyRole("AUTOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/v1/preguntas/**").hasAnyRole("AUTOR", "REVISOR", "ADMINISTRADOR")

                // Cualquier otra ruta bajo /api/v1 requiere JWT válido
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getWriter().write("{\"error\":\"Token JWT de Keycloak ausente, expirado o inválido\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getWriter().write("{\"error\":\"Acceso denegado: su rol no tiene permisos para realizar esta acción\"}");
                })
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
            );

        return http.build();
    }
}

