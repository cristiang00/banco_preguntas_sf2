import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;

/**
 * Servidor OIDC / JWKS (RS256) local compatible con Keycloak para ejecución sin Docker.
 * Lee automáticamente los usuarios, contraseñas, roles y cliente desde:
 *   docker/keycloak/saberpro-realm.json
 *
 * Uso: java scripts/LocalOidcServer.java
 */
public class LocalOidcServer {

    private static final String KID = "saberpro-local-key-1";
    private static final String DEFAULT_CLIENT_ID = "gateway-client";
    private static final String DEFAULT_CLIENT_SECRET = "saberpro-client-secret-2026";

    private static final class RealmUser {
        final long id;
        final String username;
        final String password;
        final String email;
        final String fullName;
        final List<String> roles;

        RealmUser(long id, String username, String password, String email, String fullName, List<String> roles) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.email = email;
            this.fullName = fullName;
            this.roles = roles;
        }
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("OIDC_PORT", "8089"));
        String issuer = "http://localhost:" + port + "/realms/saberpro";

        // 1. Generar par de claves RSA 2048 para firma RS256 real
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        String jwksJson = buildJwksJson(publicKey);

        // 2. Cargar usuarios y cliente desde docker/keycloak/saberpro-realm.json
        Map<String, RealmUser> usersByLower = new LinkedHashMap<>();
        String[] clientCreds = new String[] { DEFAULT_CLIENT_ID, DEFAULT_CLIENT_SECRET };
        loadRealmUsers(usersByLower, clientCreds);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/realms/saberpro/protocol/openid-connect/certs", ex -> {
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"method_not_allowed\"}");
                return;
            }
            sendJson(ex, 200, jwksJson);
        });

        server.createContext("/realms/saberpro/.well-known/openid-configuration", ex -> {
            String base = "http://localhost:" + port + "/realms/saberpro";
            String body = "{"
                    + "\"issuer\":\"" + escapeJson(issuer) + "\","
                    + "\"token_endpoint\":\"" + base + "/protocol/openid-connect/token\","
                    + "\"jwks_uri\":\"" + base + "/protocol/openid-connect/certs\""
                    + "}";
            sendJson(ex, 200, body);
        });

        server.createContext("/health/ready", ex -> sendJson(ex, 200, "{\"status\":\"UP\"}"));
        server.createContext("/health", ex -> sendJson(ex, 200, "{\"status\":\"UP\"}"));

        server.createContext("/realms/saberpro/protocol/openid-connect/token", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"method_not_allowed\"}");
                return;
            }
            String rawBody = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseForm(rawBody);

            String clientId = form.getOrDefault("client_id", "").trim();
            String clientSecret = form.getOrDefault("client_secret", "").trim();
            String username = form.getOrDefault("username", "").trim();
            String password = form.getOrDefault("password", "");

            if (!clientCreds[0].equals(clientId)
                    || (!clientSecret.isEmpty() && !clientCreds[1].equals(clientSecret))) {
                sendJson(ex, 401, "{\"error\":\"unauthorized_client\",\"error_description\":\"Invalid client credentials\"}");
                return;
            }

            RealmUser user = usersByLower.get(username.toLowerCase(Locale.ROOT));
            if (user == null || !user.password.equals(password)) {
                sendJson(ex, 401, "{\"error\":\"invalid_grant\",\"error_description\":\"Invalid user credentials\"}");
                return;
            }

            try {
                String token = issueJwt(issuer, user, privateKey);
                String resp = "{"
                        + "\"access_token\":\"" + token + "\","
                        + "\"token_type\":\"Bearer\","
                        + "\"expires_in\":7200,"
                        + "\"scope\":\"openid profile email roles\""
                        + "}";
                sendJson(ex, 200, resp);
            } catch (Exception e) {
                sendJson(ex, 500, "{\"error\":\"server_error\",\"error_description\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("[OK] Servidor OIDC/JWKS local (Keycloak-compatible) escuchando en http://localhost:" + port
                + " | Usuarios cargados: " + usersByLower.size());
        System.out.flush();
    }

    private static void loadRealmUsers(Map<String, RealmUser> out, String[] clientCreds) {
        Path[] candidates = new Path[] {
                Path.of("docker/keycloak/saberpro-realm.json"),
                Path.of("../docker/keycloak/saberpro-realm.json")
        };
        Path realmFile = null;
        for (Path p : candidates) {
            if (Files.exists(p)) {
                realmFile = p;
                break;
            }
        }

        if (realmFile != null) {
            try {
                String json = Files.readString(realmFile, StandardCharsets.UTF_8);
                Object parsed = new SimpleJsonParser(json).parse();
                if (parsed instanceof Map<?, ?> root) {
                    Object clientsObj = root.get("clients");
                    if (clientsObj instanceof List<?> clients) {
                        for (Object cObj : clients) {
                            if (cObj instanceof Map<?, ?> cMap) {
                                String cid = asString(cMap.get("clientId"));
                                String sec = asString(cMap.get("secret"));
                                if (cid != null && !cid.isBlank()) {
                                    clientCreds[0] = cid;
                                    if (sec != null && !sec.isBlank()) {
                                        clientCreds[1] = sec;
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    Object usersObj = root.get("users");
                    if (usersObj instanceof List<?> users) {
                        for (Object uObj : users) {
                            if (!(uObj instanceof Map<?, ?> uMap)) continue;
                            String username = asString(uMap.get("username"));
                            if (username == null || username.isBlank()) continue;
                            String email = asString(uMap.get("email"));
                            String firstName = asString(uMap.get("firstName"));
                            String lastName = asString(uMap.get("lastName"));
                            String fullName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
                            if (fullName.isEmpty()) fullName = username;

                            String password = "";
                            Object credsObj = uMap.get("credentials");
                            if (credsObj instanceof List<?> creds && !creds.isEmpty() && creds.get(0) instanceof Map<?, ?> credMap) {
                                String val = asString(credMap.get("value"));
                                if (val != null) password = val;
                            }

                            List<String> roles = new ArrayList<>();
                            Object rolesObj = uMap.get("realmRoles");
                            if (rolesObj instanceof List<?> rList) {
                                for (Object r : rList) {
                                    String rs = asString(r);
                                    if (rs != null && !rs.isBlank()) roles.add(rs.trim().toUpperCase(Locale.ROOT));
                                }
                            }
                            if (roles.isEmpty()) roles.add("AUTOR");

                            long userId = resolveUserId(username);
                            out.put(username.toLowerCase(Locale.ROOT),
                                    new RealmUser(userId, username, password,
                                            email != null ? email : username + "@unicauca.edu.co",
                                            fullName, roles));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[WARN] No se pudo leer saberpro-realm.json, usando usuarios por defecto: " + e.getMessage());
            }
        }

        // Garantizar siempre los usuarios base del sistema
        addFallbackIfAbsent(out, "admin", "Admin1234!", "admin@unicauca.edu.co", "Admin General", "ADMINISTRADOR");
        addFallbackIfAbsent(out, "autor1", "Autor1234!", "autor1@unicauca.edu.co", "Autor Uno", "AUTOR");
        addFallbackIfAbsent(out, "juan12", "Autor1234!", "juan12@unicauca.edu.co", "Juan Perez", "AUTOR");
        addFallbackIfAbsent(out, "revisor1", "Revisor1234!", "revisor1@unicauca.edu.co", "Revisor Uno", "REVISOR");
        addFallbackIfAbsent(out, "revisor2", "Revisor1234!", "revisor2@unicauca.edu.co", "Revisor Dos", "REVISOR");
    }

    private static void addFallbackIfAbsent(Map<String, RealmUser> map, String u, String p, String email, String name, String role) {
        map.putIfAbsent(u.toLowerCase(Locale.ROOT), new RealmUser(resolveUserId(u), u, p, email, name, List.of(role)));
    }

    // Mismo mapeo determinístico que KeycloakJwtExtractor / ResourceServerSecurityConfig / ApiClient
    private static long resolveUserId(String username) {
        if ("admin".equalsIgnoreCase(username)) return 1L;
        if ("autor1".equalsIgnoreCase(username)) return 2L;
        if ("juan12".equalsIgnoreCase(username)) return 3L;
        if ("revisor1".equalsIgnoreCase(username)) return 4L;
        if ("revisor2".equalsIgnoreCase(username)) return 5L;
        if ("Juan1".equalsIgnoreCase(username)) return 6L;
        if ("juan2".equalsIgnoreCase(username)) return 7L;
        if ("autor".equalsIgnoreCase(username)) return 8L;
        if ("Juanita".equalsIgnoreCase(username)) return 9L;
        if ("Juan6".equalsIgnoreCase(username)) return 10L;
        if ("Juan13".equalsIgnoreCase(username)) return 11L;
        return Math.abs(username.hashCode() % 10000L);
    }

    private static String issueJwt(String issuer, RealmUser user, PrivateKey privateKey) throws Exception {
        long now = Instant.now().getEpochSecond();
        String headerJson = "{\"alg\":\"RS256\",\"typ\":\"JWT\",\"kid\":\"" + KID + "\"}";

        StringBuilder rolesJson = new StringBuilder("[");
        for (int i = 0; i < user.roles.size(); i++) {
            if (i > 0) rolesJson.append(',');
            rolesJson.append('"').append(escapeJson(user.roles.get(i))).append('"');
        }
        rolesJson.append(']');

        String payloadJson = "{"
                + "\"iss\":\"" + escapeJson(issuer) + "\","
                + "\"sub\":\"" + escapeJson(user.username + "-kc-sub-" + user.id) + "\","
                + "\"preferred_username\":\"" + escapeJson(user.username) + "\","
                + "\"name\":\"" + escapeJson(user.fullName) + "\","
                + "\"email\":\"" + escapeJson(user.email) + "\","
                + "\"userId\":" + user.id + ","
                + "\"realm_access\":{\"roles\":" + rolesJson + "},"
                + "\"iat\":" + now + ","
                + "\"exp\":" + (now + 7200)
                + "}";

        String data = b64Url(headerJson.getBytes(StandardCharsets.UTF_8))
                + "." + b64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(data.getBytes(StandardCharsets.UTF_8));
        String sig = b64Url(signer.sign());
        return data + "." + sig;
    }

    private static String buildJwksJson(RSAPublicKey pub) {
        String n = b64Url(unsignedBytes(pub.getModulus()));
        String e = b64Url(unsignedBytes(pub.getPublicExponent()));
        return "{\"keys\":[{\"kty\":\"RSA\",\"kid\":\"" + KID + "\",\"use\":\"sig\",\"alg\":\"RS256\",\"n\":\"" + n + "\",\"e\":\"" + e + "\"}]}";
    }

    private static byte[] unsignedBytes(BigInteger bi) {
        byte[] bytes = bi.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }

    private static String b64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static Map<String, String> parseForm(String raw) {
        Map<String, String> result = new HashMap<>();
        if (raw == null || raw.isBlank()) return result;
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String k = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
                result.put(k, v);
            }
        }
        return result;
    }

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /** Parser JSON minimalista sin dependencias externas para leer saberpro-realm.json */
    private static final class SimpleJsonParser {
        private final String s;
        private int pos = 0;

        SimpleJsonParser(String s) {
            this.s = s;
        }

        Object parse() {
            skipWs();
            if (pos >= s.length()) return null;
            char c = s.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't') { pos += 4; return Boolean.TRUE; }
            if (c == 'f') { pos += 5; return Boolean.FALSE; }
            if (c == 'n') { pos += 4; return null; }
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // '{'
            skipWs();
            if (pos < s.length() && s.charAt(pos) == '}') { pos++; return map; }
            while (pos < s.length()) {
                skipWs();
                String key = parseString();
                skipWs();
                if (pos < s.length() && s.charAt(pos) == ':') pos++;
                Object val = parse();
                map.put(key, val);
                skipWs();
                if (pos < s.length() && s.charAt(pos) == ',') { pos++; continue; }
                if (pos < s.length() && s.charAt(pos) == '}') { pos++; break; }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // '['
            skipWs();
            if (pos < s.length() && s.charAt(pos) == ']') { pos++; return list; }
            while (pos < s.length()) {
                list.add(parse());
                skipWs();
                if (pos < s.length() && s.charAt(pos) == ',') { pos++; continue; }
                if (pos < s.length() && s.charAt(pos) == ']') { pos++; break; }
            }
            return list;
        }

        private String parseString() {
            StringBuilder sb = new StringBuilder();
            pos++; // '"'
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') break;
                if (c == '\\' && pos < s.length()) {
                    char esc = s.charAt(pos++);
                    switch (esc) {
                        case '"', '\\', '/' -> sb.append(esc);
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            String hex = s.substring(pos, Math.min(pos + 4, s.length()));
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        }
                        default -> sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Number parseNumber() {
            int start = pos;
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') pos++;
                else break;
            }
            String num = s.substring(start, pos);
            return num.contains(".") ? Double.parseDouble(num) : Long.parseLong(num);
        }

        private void skipWs() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }
    }
}
