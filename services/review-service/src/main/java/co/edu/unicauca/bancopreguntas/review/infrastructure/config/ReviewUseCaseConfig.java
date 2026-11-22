package co.edu.unicauca.bancopreguntas.review.infrastructure.config;

import co.edu.unicauca.bancopreguntas.review.application.ports.output.*;
import co.edu.unicauca.bancopreguntas.review.application.usecases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración Spring para instanciar los casos de uso inyectando los adaptadores
 * a través de sus puertos secundarios (HU8 - Arquitectura Hexagonal).
 */
@Configuration
public class ReviewUseCaseConfig {

    @Bean
    public AsignarRevisoresUseCase asignarRevisoresUseCase(
            RevisionRepositoryPort repositoryPort,
            PreguntasClientPort preguntasClientPort,
            LifecycleClientPort lifecycleClientPort,
            UsuariosClientPort usuariosClientPort,
            EventPublisherPort eventPublisherPort) {
        return new AsignarRevisoresUseCase(
                repositoryPort, preguntasClientPort, lifecycleClientPort, usuariosClientPort, eventPublisherPort
        );
    }

    @Bean
    public ListarAsignadasUseCase listarAsignadasUseCase(
            RevisionRepositoryPort repositoryPort,
            PreguntasClientPort preguntasClientPort) {
        return new ListarAsignadasUseCase(repositoryPort, preguntasClientPort);
    }

    @Bean
    public RegistrarEvaluacionUseCase registrarEvaluacionUseCase(
            RevisionRepositoryPort repositoryPort) {
        return new RegistrarEvaluacionUseCase(repositoryPort);
    }

    @Bean
    public RegistrarDecisionUseCase registrarDecisionUseCase(
            RevisionRepositoryPort repositoryPort,
            PreguntasClientPort preguntasClientPort,
            LifecycleClientPort lifecycleClientPort,
            UsuariosClientPort usuariosClientPort,
            EventPublisherPort eventPublisherPort) {
        return new RegistrarDecisionUseCase(
                repositoryPort, preguntasClientPort, lifecycleClientPort, usuariosClientPort, eventPublisherPort
        );
    }

    @Bean
    public ConsultarHistorialUseCase consultarHistorialUseCase(
            RevisionRepositoryPort repositoryPort,
            PreguntasClientPort preguntasClientPort) {
        return new ConsultarHistorialUseCase(repositoryPort, preguntasClientPort);
    }
}

