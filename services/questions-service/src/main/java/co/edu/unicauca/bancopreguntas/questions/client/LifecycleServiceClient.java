package co.edu.unicauca.bancopreguntas.questions.client;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    public void inicializarPregunta(Long preguntaId, Long autorId) {
        try {
            restTemplate.postForLocation(
                    lifecycleServiceUrl + "/ciclo-vida/preguntas/" + preguntaId + "/inicializar?autorId=" + autorId,
                    null
            );
        } catch (Exception e) {
            log.warn("No se pudo notificar inicialización a Lifecycle Service: {}", e.getMessage());
        }
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
            log.warn("Fallo temporal consultando estado autoritativo en Lifecycle: {}", e.getMessage());
        }
        return null;
    }

    public boolean puedeEditar(Long preguntaId) {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> resp = restTemplate.exchange(
                    lifecycleServiceUrl + "/ciclo-vida/preguntas/" + preguntaId + "/puede-editar",
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Boolean puede = (Boolean) resp.getBody().get("puedeEditar");
                return puede != null && puede;
            }
        } catch (Exception e) {
            log.warn("Fallo temporal consultando si puede editar en Lifecycle: {}", e.getMessage());
        }
        return false;
    }
}
