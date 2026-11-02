package co.edu.unicauca.bancopreguntas.review.repository;

import co.edu.unicauca.bancopreguntas.review.domain.ObservacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservacionRepository extends JpaRepository<ObservacionEntity, Long> {
    List<ObservacionEntity> findByRevisionId(Long revisionId);
    List<ObservacionEntity> findByPreguntaIdAndVisibleAutorTrue(Long preguntaId);
}
