package co.edu.unicauca.bancopreguntas.lifecycle;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Lifecycle Service API",
        version = "2.0.0",
        description = "Gestión del ciclo de vida de preguntas, patrón State y publicación de eventos (HU2)"
    )
)
public class LifecycleServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LifecycleServiceApplication.class, args);
    }
}
