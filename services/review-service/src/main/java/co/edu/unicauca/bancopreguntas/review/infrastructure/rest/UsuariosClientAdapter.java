package co.edu.unicauca.bancopreguntas.review.infrastructure.rest;

import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.UsuariosClientPort;
import co.edu.unicauca.bancopreguntas.review.client.UsersServiceClient;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de infraestructura REST que implementa el puerto de salida {@link UsuariosClientPort}
 * delegando al cliente HTTP existente {@link UsersServiceClient} (HU8 - Arquitectura Hexagonal).
 */
@Component
public class UsuariosClientAdapter implements UsuariosClientPort {

    private final UsersServiceClient usersServiceClient;

    public UsuariosClientAdapter(UsersServiceClient usersServiceClient) {
        this.usersServiceClient = usersServiceClient;
    }

    @Override
    public Optional<UserSummaryDTO> buscarPorId(Long id) {
        return usersServiceClient.buscarPorId(id);
    }
}

