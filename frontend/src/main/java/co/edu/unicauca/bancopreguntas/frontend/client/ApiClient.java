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
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
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
    // Métodos de Autenticación (HU7 - Keycloak con fallback)
    // =========================================================================

    public TokenResponseDTO login(String username, String password) throws IOException, ApiException {
        // HU7.S2: Intentar autenticación directa contra Keycloak (Direct Access Grants)
        try {
            TokenResponseDTO kcToken = loginWithKeycloak(username, password);
            if (kcToken != null) {
                this.token = kcToken.getToken();
                this.currentUser = kcToken;
                return kcToken;
            }
        } catch (ApiException e) {
            // Si Keycloak respondió rechazo por credenciales, propagar la excepción
            if (e.getStatusCode() == 400 || e.getStatusCode() == 401 || e.getStatusCode() == 403) {
                throw e;
            }
        } catch (Exception ignored) {
            // Fallback si Keycloak no está levantado en entornos de prueba locales
        }

        LoginRequestDTO request = new LoginRequestDTO(username, password);
        String json = objectMapper.writeValueAsString(request);

        HttpResponseData response = sendPost("/auth/login", json, false);
        if (response.getStatusCode() == 200) {
            TokenResponseDTO tokenResponse = objectMapper.readValue(response.getBody(), TokenResponseDTO.class);
            this.token = tokenResponse.getToken();
            this.currentUser = tokenResponse;
            return tokenResponse;
        } else {
            throw handleApiError(response);
        }
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

        String formBody = "grant_type=password"
                + "&client_id=gateway-client"
                + "&client_secret=saberpro-client-secret-2026"
                + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(formBody.getBytes(StandardCharsets.UTF_8));
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
        } else if (code == 400 || code == 401) {
            throw new ApiException(code, "Credenciales inválidas en Keycloak");
        }
        return null;
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
                Long userId = 2L;
                if ("admin".equalsIgnoreCase(username)) userId = 1L;
                else if ("autor1".equalsIgnoreCase(username)) userId = 2L;
                else if ("revisor1".equalsIgnoreCase(username)) userId = 4L;
                else if ("revisor2".equalsIgnoreCase(username)) userId = 5L;

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

    public List<PreguntaDTO> listarPreguntas(String estado, String dificultad, int page, int size)
            throws IOException, ApiException {
        StringBuilder query = new StringBuilder("?page=").append(page).append("&size=").append(size);
        if (estado != null && !estado.isBlank() && !"TODOS".equalsIgnoreCase(estado)) {
            query.append("&estado=").append(URLEncoder.encode(estado, StandardCharsets.UTF_8));
        }
        if (dificultad != null && !dificultad.isBlank() && !"TODOS".equalsIgnoreCase(dificultad)) {
            query.append("&dificultad=").append(URLEncoder.encode(dificultad, StandardCharsets.UTF_8));
        }

        HttpResponseData response = sendGet("/preguntas" + query, true);
        if (response.getStatusCode() == 200) {
            // El backend devuelve objeto paginado: { "content": [...], "totalPages": N, ... }
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("content")) {
                return objectMapper.readValue(root.get("content").toString(),
                        new TypeReference<List<PreguntaDTO>>() {});
            }
            // Fallback: si ya es un array directo
            return objectMapper.readValue(response.getBody(), new TypeReference<List<PreguntaDTO>>() {});
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

            // SSCRUM-HU4: Sincronizar proyección de estado en questions-service
            // para que la pregunta aparezca en la lista de pendientes del administrador
            try {
                sendPut("/preguntas/" + id + "/proyeccion-estado?estado=" + nuevoEstado.name(), "{}", true);
            } catch (Exception ignored) {
                // Best-effort: si falla la sincronización, el estado ya cambió en lifecycle
            }

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

    public List<RevisionItemDTO> listarRevisionesAsignadas(int page, int size, String estado, String dificultad, String competencia)
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
            if (root.has("content")) {
                return objectMapper.readValue(root.get("content").toString(),
                        new TypeReference<List<RevisionItemDTO>>() {});
            }
            return objectMapper.readValue(response.getBody(), new TypeReference<List<RevisionItemDTO>>() {});
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
            return objectMapper.readValue(response.getBody(), new TypeReference<List<String>>() {});
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
