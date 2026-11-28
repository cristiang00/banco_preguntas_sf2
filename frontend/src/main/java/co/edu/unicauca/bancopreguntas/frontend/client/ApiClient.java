package co.edu.unicauca.bancopreguntas.frontend.client;

import co.edu.unicauca.bancopreguntas.contracts.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ApiClient {

    private static final String DEFAULT_GATEWAY_URL = "http://localhost:8080/api/v1";
    private static final String DEFAULT_KEYCLOAK_TOKEN_URL = "http://localhost:8089/realms/saberpro/protocol/openid-connect/token";
    private static ApiClient instance;

    private final String baseUrl;
    private final String keycloakTokenUrl;
    private final ObjectMapper objectMapper;

    private String token;
    private TokenResponseDTO currentUser;

    public static class HttpResponseData {
        private final int statusCode;
        private final String body;

        public HttpResponseData(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() { return statusCode; }
        public String getBody() { return body; }
    }

    public ApiClient() {
        this(System.getProperty("gateway.url", System.getenv().getOrDefault("GATEWAY_URL", DEFAULT_GATEWAY_URL)));
    }

    public ApiClient(String baseUrl) {
        this(baseUrl, System.getProperty("keycloak.token.url", System.getenv().getOrDefault("KEYCLOAK_TOKEN_URL", DEFAULT_KEYCLOAK_TOKEN_URL)));
    }

    public ApiClient(String baseUrl, String keycloakTokenUrl) {
        String normalized = baseUrl != null ? baseUrl.trim() : DEFAULT_GATEWAY_URL;
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!normalized.endsWith("/api/v1")) {
            normalized = normalized + "/api/v1";
        }
        this.baseUrl = normalized;
        this.keycloakTokenUrl = keycloakTokenUrl;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public static synchronized void setInstance(ApiClient client) {
        instance = client;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public TokenResponseDTO getCurrentUser() { return currentUser; }
    public void setCurrentUser(TokenResponseDTO currentUser) { this.currentUser = currentUser; }

    public boolean isAuthenticated() { return token != null && !token.isBlank(); }

    public void logout() {
        this.token = null;
        this.currentUser = null;
    }

    // =========================================================================
    // Métodos de Autenticación (HU7 - Keycloak OIDC Direct Access Grants)
    // =========================================================================

    public TokenResponseDTO login(String username, String password) throws IOException, ApiException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new ApiException(401, "Usuario y contraseña son requeridos");
        }
        // HU7.S2: Autenticación exclusiva contra el endpoint de token de Keycloak
        TokenResponseDTO kcToken = loginWithKeycloak(username, password);
        if (kcToken != null) {
            this.token = kcToken.getToken();
            this.currentUser = kcToken;
            return kcToken;
        }
        throw new ApiException(401, "No se pudo obtener un token JWT válido desde Keycloak");
    }

    public TokenResponseDTO loginWithKeycloak(String username, String password) throws IOException, ApiException {
        URL url = URI.create(keycloakTokenUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");

        String clientId = System.getProperty("keycloak.client.id",
                System.getenv().getOrDefault("KEYCLOAK_CLIENT_ID", "gateway-client"));
        String clientSecret = System.getProperty("keycloak.client.secret",
                System.getenv().getOrDefault("KEYCLOAK_CLIENT_SECRET", "saberpro-client-secret-2026"));

        String formBody = "grant_type=password"
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(formBody.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ioEx) {
            throw new IOException("No se pudo conectar con Keycloak/OIDC en " + keycloakTokenUrl
                    + " (verifique que el puerto 8089 esté activo con Keycloak o scripts/LocalOidcServer.java)", ioEx);
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
        String body = is != null ? new String(is.readAllBytes(), StandardCharsets.UTF_8) : "";

        if (code == 200) {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);
            String accessToken = root.has("access_token") ? root.get("access_token").asText() : null;
            if (accessToken != null) {
                return parseKeycloakToken(accessToken, username);
            }
            throw new ApiException(401, "Keycloak no retornó un access_token válido");
        } else if (code == 400 || code == 401) {
            throw new ApiException(401, "Credenciales inválidas en Keycloak");
        } else if (code == 403) {
            throw new ApiException(403, "Acceso denegado por Keycloak");
        }
        throw new ApiException(code, "Error autenticando contra Keycloak (HTTP " + code + ")");
    }

    private TokenResponseDTO parseKeycloakToken(String token, String fallbackUsername) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
                com.fasterxml.jackson.databind.JsonNode payload = objectMapper.readTree(decodedPayload);

                String username = payload.has("preferred_username") ? payload.get("preferred_username").asText() : fallbackUsername;
                String role = "AUTOR";
                if (payload.has("realm_access") && payload.get("realm_access").has("roles")) {
                    for (com.fasterxml.jackson.databind.JsonNode r : payload.get("realm_access").get("roles")) {
                        String rName = r.asText().toUpperCase();
                        if ("ADMINISTRADOR".equals(rName) || "REVISOR".equals(rName) || "AUTOR".equals(rName)) {
                            role = rName;
                            break;
                        }
                    }
                }
                Long userId = null;
                if (payload.has("userId") && !payload.get("userId").isNull()) {
                    if (payload.get("userId").isNumber()) {
                        userId = payload.get("userId").asLong();
                    } else {
                        try { userId = Long.parseLong(payload.get("userId").asText().trim()); } catch (NumberFormatException ignored) {}
                    }
                }
                if (userId == null) {
                    if ("admin".equalsIgnoreCase(username)) userId = 1L;
                    else if ("autor1".equalsIgnoreCase(username)) userId = 2L;
                    else if ("juan12".equalsIgnoreCase(username)) userId = 3L;
                    else if ("revisor1".equalsIgnoreCase(username)) userId = 4L;
                    else if ("revisor2".equalsIgnoreCase(username)) userId = 5L;
                    else userId = (long) Math.abs(username.hashCode() % 10000);
                }

                String nombreCompleto = payload.has("name") ? payload.get("name").asText() : username;
                return new TokenResponseDTO(token, userId, username, nombreCompleto, role);
            }
        } catch (Exception ignored) {}
        return new TokenResponseDTO(token, 2L, fallbackUsername, fallbackUsername, "AUTOR");
    }

    // =========================================================================
    // Métodos de Preguntas (HU1, HU2, HU3, HU6)
    // =========================================================================

    public PreguntaDTO crearPregunta(PreguntaDTO dto) throws IOException, ApiException {
        String json = objectMapper.writeValueAsString(dto);
        HttpResponseData response = sendPost("/preguntas", json, true);
        if (response.getStatusCode() == 201 || response.getStatusCode() == 200) {
            return objectMapper.readValue(response.getBody(), PreguntaDTO.class);
        }
        throw handleApiError(response);
    }

    public PreguntaDTO actualizarPregunta(Long id, PreguntaDTO dto) throws IOException, ApiException {
        String json = objectMapper.writeValueAsString(dto);
        HttpResponseData response = sendPut("/preguntas/" + id, json, true);
        if (response.getStatusCode() == 200) {
            return objectMapper.readValue(response.getBody(), PreguntaDTO.class);
        }
        throw handleApiError(response);
    }

    @SuppressWarnings("unchecked")
    public List<PreguntaDTO> listarPreguntas(String estado, String dificultad, int page, int size)
            throws IOException, ApiException {
        Map<String, Object> pageData = listarPreguntasPaginado(estado, dificultad, page, size);
        return (List<PreguntaDTO>) pageData.getOrDefault("content", Collections.emptyList());
    }

    public Map<String, Object> listarPreguntasPaginado(String estado, String dificultad, int page, int size)
            throws IOException, ApiException {
        StringBuilder query = new StringBuilder("?page=").append(page).append("&size=").append(size);
        if (estado != null && !estado.isBlank() && !"TODOS".equalsIgnoreCase(estado)) {
            query.append("&estados=").append(URLEncoder.encode(estado, StandardCharsets.UTF_8));
        }
        if (dificultad != null && !dificultad.isBlank() && !"TODOS".equalsIgnoreCase(dificultad)) {
            query.append("&dificultad=").append(URLEncoder.encode(dificultad, StandardCharsets.UTF_8));
        }

        HttpResponseData response = sendGet("/preguntas" + query, true);
        if (response.getStatusCode() == 200) {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response.getBody());
            Map<String, Object> result = new LinkedHashMap<>();
            if (root.has("content")) {
                List<PreguntaDTO> items = objectMapper.readValue(root.get("content").toString(),
                        new TypeReference<List<PreguntaDTO>>() {});
                result.put("content", items);
                result.put("totalPages", root.has("totalPages") ? root.get("totalPages").asInt(1) : 1);
                result.put("totalElements", root.has("totalElements") ? root.get("totalElements").asLong(items.size()) : (long) items.size());
                result.put("currentPage", root.has("currentPage") ? root.get("currentPage").asInt(page) : page);
                return result;
            }
            List<PreguntaDTO> items = objectMapper.readValue(response.getBody(), new TypeReference<List<PreguntaDTO>>() {});
            result.put("content", items);
            result.put("totalPages", 1);
            result.put("totalElements", (long) items.size());
            result.put("currentPage", page);
            return result;
        }
        throw handleApiError(response);
    }

    public PreguntaDTO obtenerPregunta(Long id) throws IOException, ApiException {
        HttpResponseData response = sendGet("/preguntas/" + id, true);
        if (response.getStatusCode() == 200) {
            return objectMapper.readValue(response.getBody(), PreguntaDTO.class);
        }
        throw handleApiError(response);
    }

    public EstadoPreguntaDTO enviarARevision(Long id) throws IOException, ApiException {
        HttpResponseData response = sendPut("/preguntas/" + id + "/enviar-revision", "{}", true);
        if (response.getStatusCode() == 200) {
            EstadoPreguntaDTO nuevoEstado = EstadoPreguntaDTO.PENDIENTE_REVISION;
            try {
                com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("estado")) {
                    nuevoEstado = EstadoPreguntaDTO.fromString(root.get("estado").asText());
                }
            } catch (Exception ignored) {}

            return nuevoEstado;
        }
        throw handleApiError(response);
    }

    public PreguntaDTO crearNuevaVersion(Long id, PreguntaDTO dto) throws IOException, ApiException {
        String json = dto != null ? objectMapper.writeValueAsString(dto) : "{}";
        HttpResponseData response = sendPut("/preguntas/" + id + "/nueva-version", json, true);
        if (response.getStatusCode() == 200) {
            return objectMapper.readValue(response.getBody(), PreguntaDTO.class);
        }
        throw handleApiError(response);
    }

    public List<String> obtenerCatalogos(String tipo) throws IOException, ApiException {
        HttpResponseData response = sendGet("/catalogos/" + tipo, false);
        if (response.getStatusCode() == 200) {
            return objectMapper.readValue(response.getBody(), new TypeReference<List<String>>() {});
        }
        return Collections.emptyList();
    }

    // =========================================================================
    // Métodos de Asignación y Revisión (HU4, HU5)
    // =========================================================================

    public List<PreguntaDTO> listarPreguntasPendientesRevision() throws IOException, ApiException {
        HttpResponseData response = sendGet("/revisiones/pendientes", true);
        if (response.getStatusCode() == 200) {
            return objectMapper.readValue(response.getBody(), new TypeReference<List<PreguntaDTO>>() {});
        }
        throw handleApiError(response);
    }

    public List<UserSummaryDTO> listarRevisoresElegibles() throws IOException, ApiException {
        HttpResponseData response = sendGet("/usuarios/revisores", true);
        if (response.getStatusCode() == 200) {
            return objectMapper.readValue(response.getBody(), new TypeReference<List<UserSummaryDTO>>() {});
        }
        throw handleApiError(response);
    }

    public void asignarRevisores(Long preguntaId, List<Long> revisorIds) throws IOException, ApiException {
        AsignarRevisoresRequestDTO request = new AsignarRevisoresRequestDTO(preguntaId, revisorIds);
        String json = objectMapper.writeValueAsString(request);
        HttpResponseData response = sendPost("/revisiones/asignar", json, true);
        if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
            throw handleApiError(response);
        }
    }

    public List<RevisionItemDTO> listarRevisionesAsignadas() throws IOException, ApiException {
        return listarRevisionesAsignadas(0, 50, null, null, null);
    }

    @SuppressWarnings("unchecked")
    public List<RevisionItemDTO> listarRevisionesAsignadas(int page, int size, String estado, String dificultad, String competencia)
            throws IOException, ApiException {
        Map<String, Object> pageData = listarRevisionesAsignadasPaginado(page, size, estado, dificultad, competencia);
        return (List<RevisionItemDTO>) pageData.getOrDefault("content", Collections.emptyList());
    }

    public Map<String, Object> listarRevisionesAsignadasPaginado(int page, int size, String estado, String dificultad, String competencia)
            throws IOException, ApiException {
        StringBuilder sb = new StringBuilder("/revisiones/asignadas?page=").append(page).append("&size=").append(size);
        if (estado != null && !estado.isBlank() && !"Todos".equalsIgnoreCase(estado)) {
            sb.append("&estado=").append(URLEncoder.encode(estado, StandardCharsets.UTF_8));
        }
        if (dificultad != null && !dificultad.isBlank() && !"Todos".equalsIgnoreCase(dificultad)) {
            sb.append("&dificultad=").append(URLEncoder.encode(dificultad, StandardCharsets.UTF_8));
        }
        if (competencia != null && !competencia.isBlank() && !"Todos".equalsIgnoreCase(competencia)) {
            sb.append("&competencia=").append(URLEncoder.encode(competencia, StandardCharsets.UTF_8));
        }
        HttpResponseData response = sendGet(sb.toString(), true);
        if (response.getStatusCode() == 200) {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response.getBody());
            Map<String, Object> result = new LinkedHashMap<>();
            if (root.has("content")) {
                List<RevisionItemDTO> items = objectMapper.readValue(root.get("content").toString(),
                        new TypeReference<List<RevisionItemDTO>>() {});
                result.put("content", items);
                result.put("totalPages", root.has("totalPages") ? root.get("totalPages").asInt(1) : 1);
                result.put("totalElements", root.has("totalElements") ? root.get("totalElements").asLong(items.size()) : (long) items.size());
                result.put("currentPage", root.has("currentPage") ? root.get("currentPage").asInt(page) : page);
                return result;
            }
            List<RevisionItemDTO> items = objectMapper.readValue(response.getBody(), new TypeReference<List<RevisionItemDTO>>() {});
            result.put("content", items);
            result.put("totalPages", 1);
            result.put("totalElements", (long) items.size());
            result.put("currentPage", page);
            return result;
        }
        throw handleApiError(response);
    }

    public List<RevisionItemDTO> listarRevisionesAsignadasLegacy() throws IOException, ApiException {
        HttpResponseData response = sendGet("/revisiones/asignadas", true);
        if (response.getStatusCode() == 200) {
            // El backend devuelve objeto paginado: { "content": [...], "totalPages": N, ... }
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("content")) {
                return objectMapper.readValue(root.get("content").toString(),
                        new TypeReference<List<RevisionItemDTO>>() {});
            }
            return objectMapper.readValue(response.getBody(), new TypeReference<List<RevisionItemDTO>>() {});
        }
        throw handleApiError(response);
    }

    public void registrarEvaluacion(Long revisionId, String criterio, Double calificacion, String comentario, List<String> observaciones)
            throws IOException, ApiException {
        EvaluacionRequestDTO req = new EvaluacionRequestDTO();
        req.setCriterio(criterio);
        req.setCalificacion(calificacion);
        req.setComentario(comentario);
        req.setObservaciones(observaciones != null ? observaciones : Collections.emptyList());
        String json = objectMapper.writeValueAsString(req);
        HttpResponseData response = sendPost("/revisiones/" + revisionId + "/evaluacion", json, true);
        if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
            throw handleApiError(response);
        }
    }

    public void emitirDecision(Long revisionId, String decision, String observaciones)
            throws IOException, ApiException {
        List<String> obsList = (observaciones != null && !observaciones.isBlank()) ? List.of(observaciones) : Collections.emptyList();
        DecisionRequestDTO request = new DecisionRequestDTO(decision, obsList, observaciones);
        String json = objectMapper.writeValueAsString(request);
        HttpResponseData response = sendPut("/revisiones/" + revisionId + "/decision", json, true);
        if (response.getStatusCode() != 200) {
            throw handleApiError(response);
        }
    }

    public Map<String, Object> consultarHistorialRevisiones(Long preguntaId, int page, int size)
            throws IOException, ApiException {
        String path = "/revisiones/pregunta/" + preguntaId + "/historial?page=" + page + "&size=" + size;
        HttpResponseData response = sendGet(path, true);
        if (response.getStatusCode() == 200) {
            return objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        }
        throw handleApiError(response);
    }

    public List<String> obtenerObservaciones(Long preguntaId) throws IOException, ApiException {
        HttpResponseData response = sendGet("/revisiones/pregunta/" + preguntaId + "/observaciones", true);
        if (response.getStatusCode() == 200) {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response.getBody());
            if (root != null && root.isArray()) {
                List<String> observaciones = new ArrayList<>();
                for (com.fasterxml.jackson.databind.JsonNode item : root) {
                    if (item.isTextual()) {
                        observaciones.add(item.asText());
                    } else if (item.has("texto") && !item.get("texto").isNull()) {
                        observaciones.add(item.get("texto").asText());
                    }
                }
                return observaciones;
            }
        }
        return Collections.emptyList();
    }

    // =========================================================================
    // Métodos de Notificaciones y Auditoría
    // =========================================================================

    public List<NotificacionItemDTO> listarNotificaciones(String destinatario)
            throws IOException, ApiException {
        String path = "/notificaciones" + (destinatario != null ? "?destinatario=" + URLEncoder.encode(destinatario, StandardCharsets.UTF_8) : "");
        HttpResponseData response = sendGet(path, true);
        if (response.getStatusCode() == 200) {
            return objectMapper.readValue(response.getBody(), new TypeReference<List<NotificacionItemDTO>>() {});
        }
        throw handleApiError(response);
    }

    public List<AuditoriaItemDTO> listarAuditoria(String eventType)
            throws IOException, ApiException {
        String path = "/auditoria" + (eventType != null ? "?eventType=" + URLEncoder.encode(eventType, StandardCharsets.UTF_8) : "");
        HttpResponseData response = sendGet(path, true);
        if (response.getStatusCode() == 200) {
            return objectMapper.readValue(response.getBody(), new TypeReference<List<AuditoriaItemDTO>>() {});
        }
        throw handleApiError(response);
    }

    // =========================================================================
    // Métodos HTTP Utilitarios vía HttpURLConnection (Sin NIO Selector)
    // =========================================================================

    public HttpResponseData sendGet(String path, boolean authenticated) throws IOException {
        URL url = URI.create(baseUrl + path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Accept", "application/json");
        if (authenticated && token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
        String body = is != null ? new String(is.readAllBytes(), StandardCharsets.UTF_8) : "";
        return new HttpResponseData(code, body);
    }

    public HttpResponseData sendPost(String path, String jsonBody, boolean authenticated) throws IOException {
        URL url = URI.create(baseUrl + path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        if (authenticated && token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        if (jsonBody != null) {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
        }
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
        String body = is != null ? new String(is.readAllBytes(), StandardCharsets.UTF_8) : "";
        return new HttpResponseData(code, body);
    }

    public HttpResponseData sendPut(String path, String jsonBody, boolean authenticated) throws IOException {
        URL url = URI.create(baseUrl + path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        if (authenticated && token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        if (jsonBody != null) {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
        }
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
        String body = is != null ? new String(is.readAllBytes(), StandardCharsets.UTF_8) : "";
        return new HttpResponseData(code, body);
    }

    private ApiException handleApiError(HttpResponseData response) {
        String body = response.getBody();
        List<ErrorValidacionDTO> errores = new ArrayList<>();
        String message = "Error " + response.getStatusCode();

        try {
            // Intenta deserializar como ResultadoValidacionDTO
            ResultadoValidacionDTO resultado = objectMapper.readValue(body, ResultadoValidacionDTO.class);
            if (resultado.getErrores() != null && !resultado.getErrores().isEmpty()) {
                errores.addAll(resultado.getErrores());
                message = "Errores de validación (" + errores.size() + ")";
            }
        } catch (Exception ignored) {
            try {
                // Intenta deserializar como Map genérico
                Map<String, Object> map = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
                if (map.containsKey("error")) {
                    message = String.valueOf(map.get("error"));
                } else if (map.containsKey("mensaje")) {
                    message = String.valueOf(map.get("mensaje"));
                }
            } catch (Exception ignored2) {
                if (body != null && !body.isBlank()) {
                    message = body;
                }
            }
        }

        return new ApiException(response.getStatusCode(), message, body, errores);
    }
}
