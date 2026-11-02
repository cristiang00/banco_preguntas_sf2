package co.edu.unicauca.bancopreguntas.lifecycle.repository;

import co.edu.unicauca.bancopreguntas.lifecycle.domain.HistorialTransicionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialTransicionRepository extends JpaRepository<HistorialTransicionEntity, Long> {
    List<HistorialTransicionEntity> findByPreguntaIdOrderByFechaTransicionAsc(Long preguntaId);
}

