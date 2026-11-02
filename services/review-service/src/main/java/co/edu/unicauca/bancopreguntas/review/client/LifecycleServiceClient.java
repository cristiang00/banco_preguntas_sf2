package co.edu.unicauca.bancopreguntas.review.client;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LifecycleServiceClient {

    private static final Logger log = LoggerFactory.getLogger(LifecycleServiceClient.class);

    private final RestTemplate restTemplate;
    private final String lifecycleServiceUrl;

    public LifecycleServiceClient(RestTemplateBuilder builder,
                                  @Value("${services.lifecycle.url:http://localhost:8084}") String lifecycleServiceUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
        this.lifecycleServiceUrl = lifecycleServiceUrl;
    }

    public LifecycleServiceClient(RestTemplate restTemplate, String lifecycleServiceUrl) {
        this.restTemplate = restTemplate;
        this.lifecycleServiceUrl = lifecycleServiceUrl;
    }

    public EstadoPreguntaDTO obtenerEstado(Long preguntaId) {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> resp = restTemplate.exchange(
                    lifecycleServiceUrl + "/ciclo-vida/preguntas/" + preguntaId + "/estado",
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                String estadoStr = (String) resp.getBody().get("estado");
                return EstadoPreguntaDTO.fromString(estadoStr);
            }
        } catch (Exception e) {
            log.warn("Error consultando estado de pregunta en Lifecycle: {}", e.getMessage());
        }
        return EstadoPreguntaDTO.PENDIENTE_REVISION;
    }

    public void aplicarDecision(Long preguntaId, String decision, Long userId, String autorEmail,
                                String contexto, List<String> observaciones, String correlationId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", String.valueOf(userId));
            if (correlationId != null) headers.set("X-Correlation-Id", correlationId);

            Map<String, Object> body = new HashMap<>();
            body.put("decision", decision);
            body.put("autorEmail", autorEmail);
            body.put("contexto", contexto);
            body.put("observaciones", observaciones);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.put(lifecycleServiceUrl + "/ciclo-vida/preguntas/" + preguntaId + "/decision", request);
        } catch (Exception e) {
            log.error("Fallo aplicando cambio de estado en Lifecycle Service: {}", e.getMessage());
            throw new IllegalStateException("No se pudo actualizar el estado en el servicio de ciclo de vida: " + e.getMessage());
        }
    }
}

