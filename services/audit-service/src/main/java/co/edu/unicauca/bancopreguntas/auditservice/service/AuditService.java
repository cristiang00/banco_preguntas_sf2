package co.edu.unicauca.bancopreguntas.auditservice.service;

import co.edu.unicauca.bancopreguntas.auditservice.model.RegistroAuditoriaEntity;
import co.edu.unicauca.bancopreguntas.auditservice.repository.AuditRepository;
import co.edu.unicauca.bancopreguntas.contracts.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional
    public Optional<RegistroAuditoriaEntity> registrarEvento(DomainEvent event) {
        if (event == null) return Optional.empty();

        // 1. Verificación de Idempotencia
        if (auditRepository.existsByEventoId(event.getEventId())) {
            log.warn("[AuditService] Evento '{}' id='{}' ya registrado previamente. Omitiendo duplicado.",
                    event.getEventType(), event.getEventId());
            return Optional.empty();
        }

        Long usuarioId = null;
        String usuarioNombre = null;
        String accion = event.getEventType();
        StringBuilder detalles = new StringBuilder();

        if (event instanceof PreguntaEnviadaARevisionEvent pere) {
            usuarioId = pere.getAutorId();
            usuarioNombre = pere.getAutorNombre();
            accion = "ENVIO_A_REVISION";
            detalles.append("Pregunta #").append(pere.getPreguntaId())
                    .append(" enviada a revisión técnica. Contexto: ").append(pere.getContexto());
        } else if (event instanceof RevisoresAsignadosEvent rae) {
            usuarioId = rae.getAutorId();
            usuarioNombre = rae.getAutorNombre();
            accion = "ASIGNACION_REVISORES";
            detalles.append("Asignados ").append(rae.getRevisores() != null ? rae.getRevisores().size() : 0)
                    .append(" revisores a la pregunta #").append(rae.getPreguntaId()).append(". Revisores: [");
            if (rae.getRevisores() != null) {
                rae.getRevisores().forEach(r -> detalles.append(r.getNombreCompleto()).append(" (ID:").append(r.getRevisorId()).append("), "));
            }
            detalles.append("]");
        } else if (event instanceof PreguntaAprobadaEvent pae) {
            usuarioId = pae.getAutorId();
            usuarioNombre = pae.getAutorNombre();
            accion = "APROBACION_PREGUNTA";
            detalles.append("Pregunta #").append(pae.getPreguntaId()).append(" APROBADA.")
                    .append(" Comentario: ").append(pae.getComentarioGeneral());
        } else if (event instanceof PreguntaRechazadaEvent pre) {
            usuarioId = pre.getAutorId();
            usuarioNombre = pre.getAutorNombre();
            accion = "RECHAZO_PREGUNTA";
            detalles.append("Pregunta #").append(pre.getPreguntaId()).append(" RECHAZADA.")
                    .append(" Observaciones: ").append(pre.getObservaciones());
        } else {
            detalles.append("Evento genérico: ").append(event.getEventType())
                    .append(" sobre agregada: ").append(event.getAggregateId());
        }

        RegistroAuditoriaEntity entidad = new RegistroAuditoriaEntity(
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getCorrelationId(),
                usuarioId,
                usuarioNombre,
                accion,
                detalles.toString(),
                event.getOccurredOn() != null ? event.getOccurredOn() : LocalDateTime.now(),
                LocalDateTime.now()
        );

        RegistroAuditoriaEntity guardado = auditRepository.save(entidad);
        log.info("[AuditService] Auditado evento '{}' aggregateId='{}' accion='{}'",
                event.getEventType(), event.getAggregateId(), accion);
        return Optional.of(guardado);
    }

    public List<RegistroAuditoriaEntity> listarTodos() {
        return auditRepository.findAllByOrderByFechaRegistroDesc();
    }

    public List<RegistroAuditoriaEntity> listarPorPregunta(Long preguntaId) {
        return auditRepository.findByAggregateIdOrderByFechaRegistroDesc(String.valueOf(preguntaId));
    }

    public List<RegistroAuditoriaEntity> listarPorTipoEvento(String eventType) {
        return auditRepository.findByEventTypeOrderByFechaRegistroDesc(eventType);
    }

    public Optional<RegistroAuditoriaEntity> buscarPorId(Long id) {
        return auditRepository.findById(id);
    }
}

