package co.edu.unicauca.bancopreguntas.review.client;

import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class QuestionsServiceClient {

    private static final Logger log = LoggerFactory.getLogger(QuestionsServiceClient.class);

    private final RestTemplate restTemplate;
    private final String questionsServiceUrl;

    public QuestionsServiceClient(RestTemplateBuilder builder,
                                  @Value("${services.questions.url:http://localhost:8082}") String questionsServiceUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
        this.questionsServiceUrl = questionsServiceUrl;
    }

    public QuestionsServiceClient(RestTemplate restTemplate, String questionsServiceUrl) {
        this.restTemplate = restTemplate;
        this.questionsServiceUrl = questionsServiceUrl;
    }

    public Optional<PreguntaDTO> obtenerPregunta(Long id) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Role", "ADMINISTRADOR");
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<PreguntaDTO> resp = restTemplate.exchange(
                    questionsServiceUrl + "/preguntas/" + id,
                    HttpMethod.GET,
                    entity,
                    PreguntaDTO.class
            );
            return Optional.ofNullable(resp.getBody());
        } catch (Exception e) {
            log.warn("No se pudo obtener pregunta {} de Questions Service: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public List<PreguntaDTO> listarPreguntasPendientes() {
        try {
            ResponseEntity<List<PreguntaDTO>> resp = restTemplate.exchange(
                    questionsServiceUrl + "/preguntas/pendientes",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<PreguntaDTO>>() {}
            );
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("No se pudo listar preguntas pendientes de Questions Service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void actualizarProyeccionEstado(Long id, String nuevoEstado) {
        try {
            restTemplate.put(questionsServiceUrl + "/preguntas/" + id + "/proyeccion-estado?estado=" + nuevoEstado, null);
        } catch (Exception e) {
            log.warn("No se pudo sincronizar proyección de estado en Questions Service: {}", e.getMessage());
        }
    }
}
