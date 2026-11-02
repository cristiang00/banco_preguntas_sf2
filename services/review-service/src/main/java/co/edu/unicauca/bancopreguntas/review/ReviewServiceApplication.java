package co.edu.unicauca.bancopreguntas.review;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Review Service API",
        version = "2.0.0",
        description = "Asignación de revisores (1 a 3), evaluaciones, observaciones y decisiones con consenso (HU4, HU5)"
    )
)
public class ReviewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}
