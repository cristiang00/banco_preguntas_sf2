package co.edu.unicauca.bancopreguntas.review.client;

import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
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
import java.util.Optional;

@Component
public class UsersServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UsersServiceClient.class);

    private final RestTemplate restTemplate;
    private final String usersServiceUrl;

    public UsersServiceClient(RestTemplateBuilder builder,
                              @Value("${services.users.url:http://localhost:8081}") String usersServiceUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
        this.usersServiceUrl = usersServiceUrl;
    }

    public UsersServiceClient(RestTemplate restTemplate, String usersServiceUrl) {
        this.restTemplate = restTemplate;
        this.usersServiceUrl = usersServiceUrl;
    }

    public List<UserSummaryDTO> listarRevisores() {
        try {
            ResponseEntity<List<UserSummaryDTO>> resp = restTemplate.exchange(
                    usersServiceUrl + "/usuarios/revisores",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UserSummaryDTO>>() {}
            );
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error consultando revisores en Users Service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<UserSummaryDTO> buscarPorId(Long id) {
        try {
            ResponseEntity<UserSummaryDTO> resp = restTemplate.getForEntity(
                    usersServiceUrl + "/usuarios/" + id,
                    UserSummaryDTO.class
            );
            return Optional.ofNullable(resp.getBody());
        } catch (Exception e) {
            log.warn("Usuario {} no encontrado o Users Service no responde", id);
            return Optional.empty();
        }
    }
}
