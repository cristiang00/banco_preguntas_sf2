package co.edu.unicauca.bancopreguntas.notificationservice.service;

import co.edu.unicauca.bancopreguntas.contracts.events.*;
import co.edu.unicauca.bancopreguntas.notificationservice.adapter.EmailDeliveryException;
import co.edu.unicauca.bancopreguntas.notificationservice.adapter.EmailSender;
import co.edu.unicauca.bancopreguntas.notificationservice.factory.*;
import co.edu.unicauca.bancopreguntas.notificationservice.model.NotificacionEntity;
import co.edu.unicauca.bancopreguntas.notificationservice.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private static final int MAX_RETRIES = 3;

    private final NotificacionRepository notificacionRepository;
    private final NotificacionFactoryProvider factoryProvider;
    private final EmailSender emailSender;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               NotificacionFactoryProvider factoryProvider,
                               EmailSender emailSender) {
        this.notificacionRepository = notificacionRepository;
        this.factoryProvider = factoryProvider;
        this.emailSender = emailSender;
    }

    /**
     * Procesa un evento de dominio, genera la notificación con Factory Method,
     * aplica deduplicación idempotente y envía el email mediante el Adapter con reintentos.
     */
    @Transactional
    public List<NotificacionEntity> procesarEvento(DomainEvent event) {
        if (event == null) return Collections.emptyList();

        log.info("[NotificacionService] Recibido evento tipo '{}' id='{}'", event.getEventType(), event.getEventId());
        List<NotificacionEntity> resultado = new ArrayList<>();

        if (event instanceof RevisoresAsignadosEvent rae) {
            Map<String, Object> data = new HashMap<>();
            data.put("preguntaId", rae.getPreguntaId());
            data.put("tituloPregunta", rae.getContexto() != null ? rae.getContexto() : ("Pregunta #" + rae.getPreguntaId()));

            if (rae.getRevisores() != null) {
                for (RevisoresAsignadosEvent.RevisorInfo rev : rae.getRevisores()) {
                    String destinatario = (rev.getEmail() != null && !rev.getEmail().isBlank())
                            ? rev.getEmail()
                            : ("revisor" + rev.getRevisorId() + "@saberpro.unicauca.edu.co");
                    resultado.add(enviarNotificacionConReintentos(
                            rae.getEventId(),
                            rae.getCorrelationId(),
                            destinatario,
                            "ASIGNACION_REVISION",
                            data
                    ));
                }
            }
        } else if (event instanceof PreguntaAprobadaEvent pae) {
            Map<String, Object> data = new HashMap<>();
            data.put("preguntaId", pae.getPreguntaId());
            data.put("observaciones", pae.getComentarioGeneral());

            String destinatario = (pae.getAutorEmail() != null && !pae.getAutorEmail().isBlank())
                    ? pae.getAutorEmail()
                    : ("autor" + pae.getAutorId() + "@saberpro.unicauca.edu.co");

            resultado.add(enviarNotificacionConReintentos(
                    pae.getEventId(),
                    pae.getCorrelationId(),
                    destinatario,
                    "PREGUNTA_APROBADA",
                    data
            ));
        } else if (event instanceof PreguntaRechazadaEvent pre) {
            Map<String, Object> data = new HashMap<>();
            data.put("preguntaId", pre.getPreguntaId());
            String motivo = (pre.getObservaciones() != null && !pre.getObservaciones().isEmpty())
                    ? String.join("\n- ", pre.getObservaciones())
                    : "Pregunta rechazada tras revisión de pares.";
            data.put("motivoRechazo", motivo);

            String destinatario = (pre.getAutorEmail() != null && !pre.getAutorEmail().isBlank())
                    ? pre.getAutorEmail()
                    : ("autor" + pre.getAutorId() + "@saberpro.unicauca.edu.co");

            resultado.add(enviarNotificacionConReintentos(
                    pre.getEventId(),
                    pre.getCorrelationId(),
                    destinatario,
                    "PREGUNTA_RECHAZADA",
                    data
            ));
        } else {
            log.info("[NotificacionService] Evento '{}' no requiere notificación por correo.", event.getEventType());
        }

        return resultado;
    }

    /**
     * Envía una notificación con validación de idempotencia (deduplicación) y reintentos ante fallas.
     */
    @Transactional
    public NotificacionEntity enviarNotificacionConReintentos(String eventoId, String correlacionId,
                                                             String destinatario, String tipo,
                                                             Map<String, Object> data) {
        // 1. Verificación de Idempotencia / Deduplicación
        if (notificacionRepository.existsByEventoIdAndDestinatario(eventoId, destinatario)) {
            log.warn("[NotificacionService] Evento duplicado detectado: eventoId='{}', destinatario='{}'. Omitiendo envío.",
                    eventoId, destinatario);
            NotificacionEntity duplicada = new NotificacionEntity(
                    eventoId, correlacionId, destinatario,
                    "Notificación Duplicada", "Ignorada por idempotencia",
                    tipo, "DUPLICADA_IGNORADA", 0,
                    LocalDateTime.now(), null, "Evento ya procesado previamente"
            );
            return duplicada;
        }

        // 2. Creación del mensaje mediante Factory Method (GoF)
        NotificacionFactory factory = factoryProvider.getFactory(tipo);
        if (factory == null) {
            log.error("[NotificacionService] No existe fábrica registrada para el tipo '{}'", tipo);
            NotificacionEntity fallida = new NotificacionEntity(
                    eventoId, correlacionId, destinatario,
                    "Error Factory", "No factory for " + tipo,
                    tipo, "FALLIDA", 0,
                    LocalDateTime.now(), null, "Fábrica de notificación inexistente"
            );
            return notificacionRepository.save(fallida);
        }

        Notificacion notificacion = factory.crearNotificacion(destinatario, data);

        // 3. Envío con reintentos usando Adapter (GoF)
        int intentos = 0;
        boolean exito = false;
        String errorDetalle = null;

        while (intentos < MAX_RETRIES && !exito) {
            intentos++;
            try {
                exito = emailSender.sendEmail(
                        notificacion.getDestinatario(),
                        notificacion.getAsunto(),
                        notificacion.getCuerpo()
                );
            } catch (EmailDeliveryException e) {
                errorDetalle = e.getMessage();
                log.warn("[NotificacionService] Intento {}/{} fallido al enviar correo a '{}': {}",
                        intentos, MAX_RETRIES, destinatario, e.getMessage());
            }
        }

        NotificacionEntity entidad = new NotificacionEntity(
                eventoId,
                correlacionId,
                notificacion.getDestinatario(),
                notificacion.getAsunto(),
                notificacion.getCuerpo(),
                notificacion.getTipo(),
                exito ? "ENVIADA" : "FALLIDA",
                intentos,
                LocalDateTime.now(),
                exito ? LocalDateTime.now() : null,
                errorDetalle
        );

        return notificacionRepository.save(entidad);
    }

    public List<NotificacionEntity> listarTodas() {
        return notificacionRepository.findAllByOrderByFechaCreacionDesc();
    }

    public List<NotificacionEntity> listarPorDestinatario(String destinatario) {
        return notificacionRepository.findByDestinatarioOrderByFechaCreacionDesc(destinatario);
    }

    public Optional<NotificacionEntity> buscarPorId(Long id) {
        return notificacionRepository.findById(id);
    }
}
