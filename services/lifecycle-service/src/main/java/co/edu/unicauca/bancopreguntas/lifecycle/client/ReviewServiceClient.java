package co.edu.unicauca.bancopreguntas.lifecycle.client;

import co.edu.unicauca.bancopreguntas.lifecycle.security.ResourceServerSecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Cliente REST hacia Review Service para obtener los correos de los revisores
 * de rondas anteriores al reenviar una versión mejorada (HU6.S5.2).
 */
@Component
public class ReviewServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceClient.class);

    private final RestTemplate restTemplate;
    private final String reviewServiceUrl;

    public ReviewServiceClient(RestTemplateBuilder builder,
                               @Value("${services.review.url:http://localhost:8085}") String reviewServiceUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
        this.restTemplate.getInterceptors().add(ResourceServerSecurityConfig.bearerForwardingInterceptor());
        this.reviewServiceUrl = reviewServiceUrl;
    }

    public List<String> obtenerEmailsRevisoresAnteriores(Long preguntaId) {
        try {
            ResponseEntity<List<String>> resp = restTemplate.exchange(
                    reviewServiceUrl + "/revisiones/pregunta/" + preguntaId + "/revisores-emails",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {}
            );
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("[ReviewServiceClient] No se pudieron consultar revisores previos para pregunta #{}: {}",
                    preguntaId, e.getMessage());
            return Collections.emptyList();
        }
    }
}

