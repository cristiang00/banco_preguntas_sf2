package co.edu.unicauca.bancopreguntas.domain.repositories;

import com.unicauca.taller2.usuarios.model.Usuario;
import java.util.List;

public interface AsignacionRevisorRepository {
    void asignar(int preguntaId, int revisorId);
    List<Usuario> obtenerRevisores(int preguntaId);
    boolean existeAsignacion(int preguntaId, int revisorId);
}
