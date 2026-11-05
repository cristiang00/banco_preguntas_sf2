package co.edu.unicauca.bancopreguntas.lifecycle.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    public void actualizarProyeccionEstado(Long id, String nuevoEstado) {
        try {
            restTemplate.put(questionsServiceUrl + "/preguntas/" + id + "/proyeccion-estado?estado=" + nuevoEstado, null);
            log.info("[QuestionsServiceClient] Proyección sincronizada en Questions Service para pregunta #{}: {}", id, nuevoEstado);
        } catch (Exception e) {
            log.warn("[QuestionsServiceClient] No se pudo sincronizar proyección de estado en Questions Service para pregunta #{}: {}", id, e.getMessage());
        }
    }
}

