package co.edu.unicauca.bancopreguntas.notificationservice.repository;

import co.edu.unicauca.bancopreguntas.notificationservice.model.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<NotificacionEntity, Long> {

    boolean existsByEventoIdAndDestinatario(String eventoId, String destinatario);

    List<NotificacionEntity> findByDestinatarioOrderByFechaCreacionDesc(String destinatario);

    List<NotificacionEntity> findAllByOrderByFechaCreacionDesc();
}
