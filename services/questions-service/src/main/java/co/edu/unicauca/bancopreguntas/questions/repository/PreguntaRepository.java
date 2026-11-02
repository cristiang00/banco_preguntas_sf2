package co.edu.unicauca.bancopreguntas.questions.repository;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.questions.domain.PreguntaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<PreguntaEntity, Long>, JpaSpecificationExecutor<PreguntaEntity> {

    Page<PreguntaEntity> findByAutorId(Long autorId, Pageable pageable);

    List<PreguntaEntity> findByEstadoProyeccion(EstadoPreguntaDTO estado);
}

