package co.edu.unicauca.bancopreguntas.gateway.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validador centralizado de tokens JWT en API Gateway (HU7 - Sprint 3).
 * Soporta validación de tokens RS256 emitidos por Keycloak vía JWKS,
 * así como tokens simétricos HS256 para entornos de prueba y retrocompatibilidad.
 */
@Component
public class JwtValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtValidator.class);

    private final SecretKey hmacKey;
    private final String jwksUrl;
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    private PublicKey fallbackRsaPublicKey;

    public JwtValidator(@Value("${jwt.secret:SaberProSecretKey2026SuperSecureMustBeAtLeast256BitsLongForHMACSHA256}") String secret,
                        @Value("${keycloak.jwks-url:http://localhost:8089/realms/saberpro/protocol/openid-connect/certs}") String jwksUrl) {
        this.hmacKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwksUrl = jwksUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public JwtValidator(String secret) {
        this(secret, "http://localhost:8089/realms/saberpro/protocol/openid-connect/certs");
    }

    /**
     * Permite inyectar una clave pública RSA para pruebas unitarias sin levantar Keycloak.
     */
    public void setRsaPublicKey(PublicKey key) {
        this.fallbackRsaPublicKey = key;
    }

    public Claims validateAndExtractClaims(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        return Jwts.parser()
                .keyLocator(this::locateKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            validateAndExtractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Key locateKey(Header header) {
        String alg = header.getAlgorithm();
        if (alg != null && alg.startsWith("RS")) {
            String kid = (String) header.get("kid");
            if (fallbackRsaPublicKey != null) {
                return fallbackRsaPublicKey;
            }
            if (kid != null) {
                PublicKey cached = keyCache.get(kid);
                if (cached != null) return cached;
            }
            // Intentar cargar desde el endpoint JWKS de Keycloak
            refreshJwks();
            if (kid != null && keyCache.containsKey(kid)) {
                return keyCache.get(kid);
            }
            if (!keyCache.isEmpty()) {
                return keyCache.values().iterator().next();
            }
            if (fallbackRsaPublicKey != null) {
                return fallbackRsaPublicKey;
            }
            throw new IllegalStateException("No se pudo obtener clave pública RSA para verificar token Keycloak (kid=" + kid + ")");
        }

        // Por defecto usar clave HMAC simétrica
        return hmacKey;
    }

    private synchronized void refreshJwks() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUrl))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode keysNode = root.get("keys");
                if (keysNode != null && keysNode.isArray()) {
                    for (JsonNode keyNode : keysNode) {
                        String kid = keyNode.has("kid") ? keyNode.get("kid").asText() : null;
                        String kty = keyNode.has("kty") ? keyNode.get("kty").asText() : null;
                        if ("RSA".equalsIgnoreCase(kty) && keyNode.has("n") && keyNode.has("e")) {
                            String n = keyNode.get("n").asText();
                            String e = keyNode.get("e").asText();
                            PublicKey pubKey = createRsaPublicKey(n, e);
                            if (kid != null && pubKey != null) {
                                keyCache.put(kid, pubKey);
                            }
                        }
                    }
                    log.info("[JwtValidator] Claves JWKS de Keycloak actualizadas exitosamente: {} claves", keyCache.size());
                }
            }
        } catch (Exception e) {
            log.warn("[JwtValidator] No se pudo conectar al endpoint JWKS de Keycloak ({})", e.getMessage());
        }
    }

    private PublicKey createRsaPublicKey(String nStr, String eStr) {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(nStr);
            byte[] eBytes = Base64.getUrlDecoder().decode(eStr);
            BigInteger modulus = new BigInteger(1, nBytes);
            BigInteger exponent = new BigInteger(1, eBytes);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception ex) {
            log.error("[JwtValidator] Error construyendo clave pública RSA desde JWKS: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Extrae el rol estandarizado de la aplicación (ADMINISTRADOR, REVISOR, AUTOR)
     * inspeccionando claims directos o los roles de Keycloak (realm_access.roles).
     */
    public String extractRole(Claims claims) {
        if (claims == null) return null;

        // 1. Claim directo "role"
        Object directRole = claims.get("role");
        if (directRole instanceof String strRole && !strRole.isBlank()) {
            return strRole.trim().toUpperCase();
        }

        // 2. Claim de Keycloak "realm_access" -> "roles"
        Object realmAccessObj = claims.get("realm_access");
        if (realmAccessObj instanceof Map<?, ?> realmAccess) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof List<?> rolesList) {
                if (hasRole(rolesList, "ADMINISTRADOR")) return "ADMINISTRADOR";
                if (hasRole(rolesList, "REVISOR")) return "REVISOR";
                if (hasRole(rolesList, "AUTOR")) return "AUTOR";
            }
        }

        // 3. Claim genérico "roles" (lista de strings)
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> rolesList) {
            if (hasRole(rolesList, "ADMINISTRADOR")) return "ADMINISTRADOR";
            if (hasRole(rolesList, "REVISOR")) return "REVISOR";
            if (hasRole(rolesList, "AUTOR")) return "AUTOR";
        }

        return null;
    }

    private boolean hasRole(List<?> roles, String targetRole) {
        for (Object r : roles) {
            if (r != null && targetRole.equalsIgnoreCase(r.toString().trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extrae el nombre de usuario de las claims estándar de Keycloak o JWT.
     */
    public String extractUsername(Claims claims) {
        if (claims == null) return null;
        Object preferred = claims.get("preferred_username");
        if (preferred instanceof String str && !str.isBlank()) {
            return str.trim();
        }
        Object sub = claims.getSubject();
        if (sub instanceof String str && !str.isBlank()) {
            return str.trim();
        }
        return "usuario";
    }

    /**
     * Extrae o infiere el identificador numérico de usuario para propagación a microservicios.
     */
    public Long extractUserId(Claims claims) {
        if (claims == null) return null;
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number num) {
            return num.longValue();
        }
        if (userIdObj instanceof String str) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {}
        }

        // Mapeo determinista según usuario de Keycloak si no tiene claim numérico
        String username = extractUsername(claims);
        if ("admin".equalsIgnoreCase(username)) return 1L;
        if ("autor1".equalsIgnoreCase(username)) return 2L;
        if ("revisor1".equalsIgnoreCase(username)) return 4L;
        if ("revisor2".equalsIgnoreCase(username)) return 5L;

        return (long) Math.abs(username.hashCode() % 10000);
    }
}
