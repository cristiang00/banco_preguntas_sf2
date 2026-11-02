package co.edu.unicauca.bancopreguntas.auditservice.repository;

import co.edu.unicauca.bancopreguntas.auditservice.model.RegistroAuditoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<RegistroAuditoriaEntity, Long> {

    boolean existsByEventoId(String eventoId);

    List<RegistroAuditoriaEntity> findByAggregateIdOrderByFechaRegistroDesc(String aggregateId);

    List<RegistroAuditoriaEntity> findByEventTypeOrderByFechaRegistroDesc(String eventType);

    List<RegistroAuditoriaEntity> findAllByOrderByFechaRegistroDesc();
}
