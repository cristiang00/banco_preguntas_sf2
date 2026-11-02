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
    private static ApiClient instance;

    private final String baseUrl;
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
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
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
    // Métodos de Autenticación
    // =========================================================================

    public TokenResponseDTO login(String username, String password) throws IOException, ApiException {
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

    // =========================================================================
    // Métodos de Preguntas (HU1, HU2, HU3)
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
            return objectMapper.readValue(response.getBody(), EstadoPreguntaDTO.class);
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
        HttpResponseData response = sendGet("/revisiones/asignadas", true);
        if (response.getStatusCode() == 200) {
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
