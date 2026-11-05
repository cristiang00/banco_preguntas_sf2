package co.edu.unicauca.bancopreguntas.validation.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
public class RestCatalogClient implements CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(RestCatalogClient.class);

    private final RestTemplate restTemplate;
    private final String questionsServiceUrl;

    // Catálogos base de respaldo para evitar bloqueos e indisponibilidad (resiliencia y timeouts)
    private static final Set<String> COMPETENCIAS_DEFAULT = Set.of(
            "LECTURA CRÍTICA", "LECTURA CRITICA",
            "RAZONAMIENTO CUANTITATIVO",
            "COMPETENCIAS CIUDADANAS",
            "COMUNICACIÓN ESCRITA", "COMUNICACION ESCRITA",
            "INGLÉS", "INGLES",
            "DISEÑO DE SOFTWARE", "DISENO DE SOFTWARE"
    );

    public RestCatalogClient(RestTemplateBuilder builder,
                             @Value("${services.questions.url:http://localhost:8082}") String questionsServiceUrl) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(1500))
                .setReadTimeout(Duration.ofMillis(2000))
                .build();
        this.questionsServiceUrl = questionsServiceUrl;
    }

    @Override
    public boolean esCompetenciaValida(String competencia) {
        if (competencia == null || competencia.isBlank()) return false;
        try {
            ResponseEntity<List<String>> resp = restTemplate.exchange(
                    questionsServiceUrl + "/catalogos/competencias",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {}
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody().stream().anyMatch(c -> c.equalsIgnoreCase(competencia.trim()));
            }
        } catch (Exception e) {
            log.warn("Fallo temporal consultando catálogo de competencias en Questions Service: {}. Usando catálogo base resiliente.", e.getMessage());
        }
        return COMPETENCIAS_DEFAULT.contains(competencia.trim().toUpperCase());
    }

    @Override
    public boolean esTemaValido(String tema) {
        if (tema == null || tema.isBlank()) return false;
        try {
            ResponseEntity<List<String>> resp = restTemplate.exchange(
                    questionsServiceUrl + "/catalogos/temas",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {}
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody().stream().anyMatch(t -> t.equalsIgnoreCase(tema.trim()));
            }
        } catch (Exception e) {
            log.warn("Fallo temporal consultando catálogo de temas: {}. Aceptando tema no vacío por resiliencia.", e.getMessage());
        }
        return !tema.trim().isEmpty();
    }

    @Override
    public boolean esSubtemaValido(String subtema) {
        if (subtema == null || subtema.isBlank()) return false;
        try {
            ResponseEntity<List<String>> resp = restTemplate.exchange(
                    questionsServiceUrl + "/catalogos/subtemas",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {}
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody().stream().anyMatch(s -> s.equalsIgnoreCase(subtema.trim()));
            }
        } catch (Exception e) {
            log.warn("Fallo temporal consultando catálogo de subtemas: {}. Aceptando subtema no vacío por resiliencia.", e.getMessage());
        }
        return !subtema.trim().isEmpty();
    }
}
