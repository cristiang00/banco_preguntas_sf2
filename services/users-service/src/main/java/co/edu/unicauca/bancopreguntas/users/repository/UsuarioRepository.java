package co.edu.unicauca.bancopreguntas.users.repository;

import co.edu.unicauca.bancopreguntas.users.domain.EstadoUsuario;
import co.edu.unicauca.bancopreguntas.users.domain.Rol;
import co.edu.unicauca.bancopreguntas.users.domain.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByNombreUsuarioIgnoreCase(String nombreUsuario);

    List<UsuarioEntity> findByRolAndEstado(Rol rol, EstadoUsuario estado);

    List<UsuarioEntity> findByEstado(EstadoUsuario estado);
}

