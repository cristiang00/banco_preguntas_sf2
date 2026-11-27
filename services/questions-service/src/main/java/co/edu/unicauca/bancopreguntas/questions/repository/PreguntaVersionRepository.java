package co.edu.unicauca.bancopreguntas.questions.repository;

import co.edu.unicauca.bancopreguntas.questions.domain.PreguntaVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaVersionRepository extends JpaRepository<PreguntaVersionEntity, Long> {
    List<PreguntaVersionEntity> findByPreguntaIdOrderByVersionAsc(Long preguntaId);
}

