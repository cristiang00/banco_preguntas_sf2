package co.edu.unicauca.bancopreguntas.gateway.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Extractor de identidad y roles desde tokens JWT verificados por Spring Security Resource Server (HU7).
 * Mapea los roles del realm de Keycloak (realm_access.roles) a roles estándar de la aplicación.
 */
@Component
public class KeycloakJwtExtractor {

    public Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        if (jwt == null) return Collections.emptyList();
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        String primaryRole = extractRole(jwt);
        if (primaryRole != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + primaryRole));
        }

        // Incluir también cualquier rol adicional del realm reconocido
        for (String role : extractAllRawRoles(jwt)) {
            String normalized = role.trim().toUpperCase();
            if ("ADMINISTRADOR".equals(normalized) || "REVISOR".equals(normalized) || "AUTOR".equals(normalized)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + normalized));
            }
        }
        return authorities;
    }

    public String extractRole(Jwt jwt) {
        if (jwt == null) return null;

        List<String> rawRoles = extractAllRawRoles(jwt);
        if (containsIgnoreCase(rawRoles, "ADMINISTRADOR")) return "ADMINISTRADOR";
        if (containsIgnoreCase(rawRoles, "REVISOR")) return "REVISOR";
        if (containsIgnoreCase(rawRoles, "AUTOR")) return "AUTOR";

        Object directRole = jwt.getClaim("role");
        if (directRole instanceof String str && !str.isBlank()) {
            return str.trim().toUpperCase();
        }
        return null;
    }

    public String extractUsername(Jwt jwt) {
        if (jwt == null) return null;
        String preferred = jwt.getClaimAsString("preferred_username");
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        String sub = jwt.getSubject();
        if (sub != null && !sub.isBlank()) {
            return sub.trim();
        }
        return "usuario";
    }

    public Long extractUserId(Jwt jwt) {
        if (jwt == null) return null;
        Object userIdObj = jwt.getClaim("userId");
        if (userIdObj instanceof Number num) {
            return num.longValue();
        }
        if (userIdObj instanceof String str) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {}
        }

        String username = extractUsername(jwt);
        if ("admin".equalsIgnoreCase(username)) return 1L;
        if ("autor1".equalsIgnoreCase(username)) return 2L;
        if ("juan12".equalsIgnoreCase(username)) return 3L;
        if ("revisor1".equalsIgnoreCase(username)) return 4L;
        if ("revisor2".equalsIgnoreCase(username)) return 5L;

        return (long) Math.abs(username.hashCode() % 10000);
    }

    private List<String> extractAllRawRoles(Jwt jwt) {
        List<String> result = new ArrayList<>();
        Object realmAccessObj = jwt.getClaim("realm_access");
        if (realmAccessObj instanceof Map<?, ?> realmAccess) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof Collection<?> roles) {
                for (Object r : roles) {
                    if (r != null) result.add(r.toString());
                }
            }
        }
        Object rolesObj = jwt.getClaim("roles");
        if (rolesObj instanceof Collection<?> roles) {
            for (Object r : roles) {
                if (r != null) result.add(r.toString());
            }
        }
        return result;
    }

    private boolean containsIgnoreCase(List<String> list, String target) {
        for (String item : list) {
            if (item != null && target.equalsIgnoreCase(item.trim())) {
                return true;
            }
        }
        return false;
    }
}

