package co.edu.unicauca.bancopreguntas.questions;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Questions Service API",
        version = "2.0.0",
        description = "Gestión, creación, persistencia propia, edición y catálogos de preguntas (HU1, HU3)"
    )
)
public class QuestionsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuestionsServiceApplication.class, args);
    }
}

