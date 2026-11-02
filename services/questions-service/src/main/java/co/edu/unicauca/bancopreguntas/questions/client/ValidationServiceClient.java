package co.edu.unicauca.bancopreguntas.questions.client;

import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.ResultadoValidacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ValidationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ValidationServiceClient.class);

    private final RestTemplate restTemplate;
    private final String validationServiceUrl;

    public ValidationServiceClient(RestTemplateBuilder builder,
                                   @Value("${services.validation.url:http://localhost:8083}") String validationServiceUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
        this.validationServiceUrl = validationServiceUrl;
    }

    public ValidationServiceClient(RestTemplate restTemplate, String validationServiceUrl) {
        this.restTemplate = restTemplate;
        this.validationServiceUrl = validationServiceUrl;
    }

    public ResultadoValidacionDTO validarPregunta(PreguntaDTO pregunta) {
        try {
            HttpEntity<PreguntaDTO> request = new HttpEntity<>(pregunta);
            ResponseEntity<ResultadoValidacionDTO> resp = restTemplate.exchange(
                    validationServiceUrl + "/validaciones/validar-pregunta",
                    HttpMethod.POST,
                    request,
                    ResultadoValidacionDTO.class
            );
            return resp.getBody() != null ? resp.getBody() : ResultadoValidacionDTO.exitoso();
        } catch (HttpClientErrorException.BadRequest e) {
            ResultadoValidacionDTO body = e.getResponseBodyAs(ResultadoValidacionDTO.class);
            return body != null ? body : ResultadoValidacionDTO.conErrores(java.util.Collections.emptyList());
        } catch (Exception e) {
            log.error("Error comunicando con Validation Service: {}", e.getMessage());
            throw new IllegalStateException("Servicio de Validación temporalmente inaccesible: " + e.getMessage());
        }
    }
}
