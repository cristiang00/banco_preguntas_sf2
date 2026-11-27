package co.edu.unicauca.bancopreguntas.lifecycle.client;

import co.edu.unicauca.bancopreguntas.lifecycle.security.ResourceServerSecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
        this.restTemplate.getInterceptors().add(ResourceServerSecurityConfig.bearerForwardingInterceptor());
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

    public Integer obtenerVersion(Long id) {
        try {
            @SuppressWarnings("unchecked")
            org.springframework.http.ResponseEntity<Map> respEntity = restTemplate.exchange(
                    questionsServiceUrl + "/preguntas/" + id,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    Map.class
            );
            Map<String, Object> resp = respEntity.getBody();
            if (resp != null && resp.get("version") != null) {
                return ((Number) resp.get("version")).intValue();
            }
        } catch (Exception e) {
            log.warn("[QuestionsServiceClient] No se pudo obtener versión de Questions Service para pregunta #{}: {}", id, e.getMessage());
        }
        return 1;
    }

    public Long obtenerAutorIdReal(Long id) {
        try {
            @SuppressWarnings("unchecked")
            org.springframework.http.ResponseEntity<Map> respEntity = restTemplate.exchange(
                    questionsServiceUrl + "/preguntas/" + id,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    Map.class
            );
            Map<String, Object> resp = respEntity.getBody();
            if (resp != null && resp.get("autorId") instanceof Number num) {
                return num.longValue();
            }
            return null;
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            throw new co.edu.unicauca.bancopreguntas.lifecycle.domain.state.AccesoDenegadoException(
                    "No tiene permisos para enviar a revisión una pregunta creada por otro autor"
            );
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("La pregunta con ID " + id + " no existe");
        } catch (Exception e) {
            log.warn("[QuestionsServiceClient] No se pudo consultar autorId en Questions Service para pregunta #{}: {}", id, e.getMessage());
            return null;
        }
    }
}
