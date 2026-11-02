package co.edu.unicauca.bancopreguntas.users;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Users & Authentication Service API",
        version = "2.0.0",
        description = "Gestión de usuarios, roles, autenticación JWT y consulta de revisores elegibles"
    )
)
public class UsersServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsersServiceApplication.class, args);
    }
}

