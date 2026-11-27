package co.edu.unicauca.bancopreguntas.questions.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerSecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${keycloak.jwks-url:http://localhost:8089/realms/saberpro/protocol/openid-connect/certs}") String jwksUrl) {
        return NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(ResourceServerSecurityConfig::extractAuthorities);
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/catalogos/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> writeJsonError(res, HttpServletResponse.SC_UNAUTHORIZED,
                        "Acceso no autorizado: se requiere un token JWT válido de Keycloak"))
                .accessDeniedHandler((req, res, e) -> writeJsonError(res, HttpServletResponse.SC_FORBIDDEN,
                        "Acceso denegado: su rol no tiene permisos para esta operación"))
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
        return http.build();
    }

    @Bean
    public OpenAPI openAPIWithBearerAuth() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components().addSecuritySchemes("BearerAuth",
                        new SecurityScheme().name("BearerAuth").type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }

    public static ClientHttpRequestInterceptor bearerForwardingInterceptor() {
        return (request, body, execution) -> {
            String token = currentBearerToken();
            if (token != null && !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                request.getHeaders().setBearerAuth(token);
            }
            return execution.execute(request, body);
        };
    }

    public static String currentBearerToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getTokenValue();
        }
        return null;
    }

    public static Long extractUserId(Jwt jwt) {
        if (jwt == null) return null;
        Object userIdObj = jwt.getClaim("userId");
        if (userIdObj instanceof Number num) return num.longValue();
        if (userIdObj instanceof String str) {
            try { return Long.parseLong(str.trim()); } catch (NumberFormatException ignored) {}
        }
        String username = extractUsername(jwt);
        if ("admin".equalsIgnoreCase(username)) return 1L;
        if ("autor1".equalsIgnoreCase(username)) return 2L;
        if ("juan12".equalsIgnoreCase(username)) return 3L;
        if ("revisor1".equalsIgnoreCase(username)) return 4L;
        if ("revisor2".equalsIgnoreCase(username)) return 5L;
        return (long) Math.abs(username.hashCode() % 10000);
    }

    public static String extractUsername(Jwt jwt) {
        if (jwt == null) return "";
        String preferred = jwt.getClaimAsString("preferred_username");
        if (preferred != null && !preferred.isBlank()) return preferred.trim();
        return jwt.getSubject() != null ? jwt.getSubject().trim() : "";
    }

    public static String extractRole(Jwt jwt) {
        if (jwt == null) return null;
        List<String> roles = extractRawRoles(jwt);
        if (containsIgnoreCase(roles, "ADMINISTRADOR")) return "ADMINISTRADOR";
        if (containsIgnoreCase(roles, "REVISOR")) return "REVISOR";
        if (containsIgnoreCase(roles, "AUTOR")) return "AUTOR";
        return null;
    }

    public static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        if (jwt == null) return authorities;
        for (String role : extractRawRoles(jwt)) {
            String norm = role.trim().toUpperCase();
            if ("ADMINISTRADOR".equals(norm) || "REVISOR".equals(norm) || "AUTOR".equals(norm)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + norm));
            }
        }
        return authorities;
    }

    private static List<String> extractRawRoles(Jwt jwt) {
        List<String> list = new ArrayList<>();
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> map && map.get("roles") instanceof Collection<?> col) {
            col.forEach(r -> { if (r != null) list.add(r.toString()); });
        }
        if (jwt.getClaim("roles") instanceof Collection<?> col) {
            col.forEach(r -> { if (r != null) list.add(r.toString()); });
        }
        String direct = jwt.getClaimAsString("role");
        if (direct != null && !direct.isBlank()) list.add(direct);
        return list;
    }

    private static boolean containsIgnoreCase(List<String> list, String target) {
        return list.stream().anyMatch(r -> r != null && target.equalsIgnoreCase(r.trim()));
    }

    private static void writeJsonError(HttpServletResponse res, int status, String message) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}

