package co.edu.unicauca.bancopreguntas.review.application.ports.output;

import co.edu.unicauca.bancopreguntas.contracts.dto.UserSummaryDTO;

import java.util.Optional;

/**
 * Puerto de salida para comunicación con el microservicio de Usuarios.
 */
public interface UsuariosClientPort {

    Optional<UserSummaryDTO> buscarPorId(Long usuarioId);
}

