package co.edu.unicauca.bancopreguntas.lifecycle.repository;

import co.edu.unicauca.bancopreguntas.lifecycle.domain.PreguntaCicloVidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreguntaCicloVidaRepository extends JpaRepository<PreguntaCicloVidaEntity, Long> {
}
