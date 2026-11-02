package co.edu.unicauca.bancopreguntas.validation.repository;

import co.edu.unicauca.bancopreguntas.validation.domain.ResultadoValidacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultadoValidacionRepository extends JpaRepository<ResultadoValidacionEntity, Long> {
}

