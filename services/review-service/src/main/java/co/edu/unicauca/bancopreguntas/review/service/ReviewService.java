package co.edu.unicauca.bancopreguntas.review.service;

import co.edu.unicauca.bancopreguntas.contracts.dto.*;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaAprobadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.RevisoresAsignadosEvent;
import co.edu.unicauca.bancopreguntas.review.client.LifecycleServiceClient;
import co.edu.unicauca.bancopreguntas.review.client.QuestionsServiceClient;
import co.edu.unicauca.bancopreguntas.review.client.UsersServiceClient;
import co.edu.unicauca.bancopreguntas.review.domain.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.EvaluacionRevisorEntity;
import co.edu.unicauca.bancopreguntas.review.domain.ObservacionEntity;
import co.edu.unicauca.bancopreguntas.review.domain.RevisionEntity;
import co.edu.unicauca.bancopreguntas.review.messaging.ReviewEventPublisher;
import co.edu.unicauca.bancopreguntas.review.repository.EvaluacionRevisorRepository;
import co.edu.unicauca.bancopreguntas.review.repository.ObservacionRepository;
import co.edu.unicauca.bancopreguntas.review.repository.RevisionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final RevisionRepository revisionRepository;
    private final EvaluacionRevisorRepository evaluacionRepository;
    private final ObservacionRepository observacionRepository;
    private final UsersServiceClient usersClient;
    private final QuestionsServiceClient questionsClient;
    private final LifecycleServiceClient lifecycleClient;
    private final ReviewEventPublisher eventPublisher;

    public ReviewService(RevisionRepository revisionRepository,
                         EvaluacionRevisorRepository evaluacionRepository,
                         ObservacionRepository observacionRepository,
                         UsersServiceClient usersClient,
                         QuestionsServiceClient questionsClient,
                         LifecycleServiceClient lifecycleClient,
                         ReviewEventPublisher eventPublisher) {
        this.revisionRepository = revisionRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.observacionRepository = observacionRepository;
        this.usersClient = usersClient;
        this.questionsClient = questionsClient;
        this.lifecycleClient = lifecycleClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public List<RevisionEntity> asignarRevisores(Long preguntaId, List<Long> revisorIds, Long adminId, String correlationId) {
        // HU4 - Validación de cantidad obligatoria (0 -> Error 400, > 3 -> Error 400)
        if (revisorIds == null || revisorIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un revisor (entre 1 y 3 revisores permitidos)");
        }
        if (revisorIds.size() > 3) {
            throw new IllegalArgumentException("Solo se permite asignar un máximo de 3 revisores por pregunta");
        }

        PreguntaDTO pregunta = questionsClient.obtenerPregunta(preguntaId)
                .orElseThrow(() -> new IllegalArgumentException("La pregunta con ID " + preguntaId + " no existe"));

        // HU4 - Exclusión del autor de su propia pregunta
        if (pregunta.getAutorId() != null && revisorIds.contains(pregunta.getAutorId())) {
            throw new IllegalArgumentException("El autor de la pregunta no puede ser asignado como revisor de su propia pregunta");
        }

        // HU4 - Verificar que el estado permita asignación
        EstadoPreguntaDTO estado = lifecycleClient.obtenerEstado(preguntaId);
        if (estado != EstadoPreguntaDTO.PENDIENTE_REVISION) {
            throw new IllegalStateException("Solo las preguntas en estado Pendiente de revisión pueden tener revisores asignados");
        }

        // Validar revisores en Users Service
        List<RevisoresAsignadosEvent.RevisorInfo> revisoresInfo = new ArrayList<>();
        List<RevisionEntity> asignacionesCreadas = new ArrayList<>();

        for (Long revId : revisorIds) {
            UserSummaryDTO user = usersClient.buscarPorId(revId)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario revisor con ID " + revId + " no existe"));

            if (!"REVISOR".equalsIgnoreCase(user.getRol())) {
                throw new IllegalArgumentException("El usuario " + user.getNombreUsuario() + " no tiene rol de Revisor");
            }

            if (!"ACTIVO".equalsIgnoreCase(user.getEstado())) {
                throw new IllegalArgumentException("El revisor " + user.getNombreUsuario() + " está inactivo");
            }

            // Prevenir asignaciones duplicadas
            if (!revisionRepository.existsByPreguntaIdAndRevisorId(preguntaId, revId)) {
                RevisionEntity rev = new RevisionEntity(preguntaId, revId, user.getNombreCompleto(), user.getEmail());
                RevisionEntity guardada = revisionRepository.save(rev);
                asignacionesCreadas.add(guardada);
            }

            revisoresInfo.add(new RevisoresAsignadosEvent.RevisorInfo(user.getId(), user.getNombreCompleto(), user.getEmail()));
        }

        // HU4 - Publicar evento RevisoresAsignados en el broker
        RevisoresAsignadosEvent event = new RevisoresAsignadosEvent(
                preguntaId,
                pregunta.getAutorId(),
                pregunta.getAutorNombre(),
                pregunta.getContexto(),
                revisoresInfo,
                correlationId
        );
        eventPublisher.publish(event);

        return asignacionesCreadas;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listarAsignadas(Long revisorId, int page, int size, String dificultad, String competencia) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<RevisionEntity> revPage = revisionRepository.findByRevisorId(revisorId, pageable);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (RevisionEntity rev : revPage.getContent()) {
            Optional<PreguntaDTO> pOpt = questionsClient.obtenerPregunta(rev.getPreguntaId());
            if (pOpt.isPresent()) {
                PreguntaDTO p = pOpt.get();

                // Filtro en memoria opcional por dificultad o competencia si se solicita
                if (dificultad != null && !"TODOS".equalsIgnoreCase(dificultad) && !dificultad.equalsIgnoreCase(p.getNivelDificultad())) {
                    continue;
                }
                if (competencia != null && !competencia.isBlank() && !competencia.equalsIgnoreCase(p.getCompetencia())) {
                    continue;
                }

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("revisionId", rev.getId());
                item.put("preguntaId", p.getId());
                item.put("contexto", p.getContexto());
                item.put("preguntaDirecta", p.getPreguntaDirecta());
                item.put("competencia", p.getCompetencia());
                item.put("tema", p.getTema());
                item.put("subtema", p.getSubtema());
                item.put("nivelDificultad", p.getNivelDificultad());
                item.put("estadoRevision", rev.getEstado().name());
                item.put("estadoPregunta", p.getEstado());
                item.put("estadoColor", p.getEstadoColor());
                item.put("fechaAsignacion", rev.getFechaAsignacion());
                item.put("opciones", p.getOpciones());
                item.put("justificacion", p.getJustificacion());
                item.put("bibliografia", p.getBibliografia());
                resultList.add(item);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", resultList);
        response.put("currentPage", revPage.getNumber());
        response.put("totalElements", revPage.getTotalElements());
        response.put("totalPages", revPage.getTotalPages());
        return response;
    }

    @Transactional
    public EvaluacionRevisorEntity registrarEvaluacion(Long revisionId, EvaluacionRequestDTO req, Long revisorId) {
        RevisionEntity rev = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("La revisión con ID " + revisionId + " no existe"));

        if (!rev.getRevisorId().equals(revisorId)) {
            throw new SecurityException("No tiene permisos para evaluar esta revisión (asignada a otro revisor)");
        }

        if (rev.getEstado().estaFinalizada()) {
            throw new IllegalStateException("Esta revisión ya fue finalizada previamente");
        }

        EvaluacionRevisorEntity eval = new EvaluacionRevisorEntity(
                revisionId,
                req.getCriterio() != null ? req.getCriterio() : "General",
                req.getCalificacion() != null ? req.getCalificacion() : 5.0,
                req.getComentario()
        );
        EvaluacionRevisorEntity guardada = evaluacionRepository.save(eval);

        // Guardar observaciones visibles para el autor
        if (req.getObservaciones() != null) {
            for (String obsText : req.getObservaciones()) {
                if (obsText != null && !obsText.trim().isEmpty()) {
                    observacionRepository.save(new ObservacionEntity(revisionId, rev.getPreguntaId(), obsText.trim()));
                }
            }
        }

        rev.setEstado(EstadoRevision.EVALUADA);
        revisionRepository.save(rev);

        return guardada;
    }

    @Transactional
    public RevisionEntity registrarDecision(Long revisionId, DecisionRequestDTO req, Long revisorId, String correlationId) {
        RevisionEntity rev = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("La revisión con ID " + revisionId + " no existe"));

        // HU5 - SSCRUM-66: Validación de asignación
        if (!rev.getRevisorId().equals(revisorId)) {
            throw new SecurityException("No tiene permisos para decidir sobre una pregunta que no le fue asignada");
        }

        // HU5 - SSCRUM-66: Validación de no re-decisión
        if (rev.getEstado().estaFinalizada()) {
            throw new IllegalStateException("Esta revisión ya se encuentra finalizada y no puede ser decidida nuevamente");
        }

        String decision = req.getDecision() != null ? req.getDecision().trim().toUpperCase() : "";
        Long preguntaId = rev.getPreguntaId();

        PreguntaDTO pregunta = questionsClient.obtenerPregunta(preguntaId)
                .orElseThrow(() -> new IllegalArgumentException("Pregunta no encontrada"));

        if ("RECHAZAR".equals(decision) || "RECHAZADA".equals(decision)) {
            // HU5 - SSCRUM-65: Para rechazar debe existir al menos una observación no vacía
            List<ObservacionEntity> obsGuardadas = observacionRepository.findByRevisionId(revisionId);
            boolean tieneObsEnRequest = req.getObservaciones() != null && req.getObservaciones().stream().anyMatch(o -> o != null && !o.trim().isEmpty());
            boolean tieneObsPrevia = !obsGuardadas.isEmpty();

            if (!tieneObsEnRequest && !tieneObsPrevia) {
                throw new IllegalArgumentException("Para rechazar la pregunta es obligatorio registrar al menos una observación justificando el motivo");
            }

            // Guardar observaciones nuevas si vienen en el request
            if (req.getObservaciones() != null) {
                for (String txt : req.getObservaciones()) {
                    if (txt != null && !txt.trim().isEmpty()) {
                        observacionRepository.save(new ObservacionEntity(revisionId, preguntaId, txt.trim()));
                    }
                }
            }

            rev.setEstado(EstadoRevision.RECHAZADA);
            rev.setFechaDecision(LocalDateTime.now());
            revisionRepository.save(rev);

            // Política de rechazo: Si un revisor rechaza con motivo, la pregunta pasa a Rechazada
            // Cerrar las demás revisiones pendientes
            List<RevisionEntity> todasRevisiones = revisionRepository.findByPreguntaId(preguntaId);
            for (RevisionEntity otra : todasRevisiones) {
                if (!otra.getId().equals(revisionId) && !otra.getEstado().estaFinalizada()) {
                    otra.setEstado(EstadoRevision.CERRADA_POR_RECHAZO);
                    otra.setFechaDecision(LocalDateTime.now());
                    revisionRepository.save(otra);
                }
            }

            List<String> todasLasObservaciones = observacionRepository.findByPreguntaIdAndVisibleAutorTrue(preguntaId)
                    .stream().map(ObservacionEntity::getTexto).collect(Collectors.toList());

            // Coordinar con Lifecycle Service
            lifecycleClient.aplicarDecision(preguntaId, "RECHAZADA", revisorId, "autor@unicauca.edu.co",
                    pregunta.getContexto(), todasLasObservaciones, correlationId);

            // Actualizar proyección en Questions Service
            questionsClient.actualizarProyeccionEstado(preguntaId, "RECHAZADA");

            // Evento PreguntaRechazada
            PreguntaRechazadaEvent rechazoEvent = new PreguntaRechazadaEvent(
                    preguntaId, pregunta.getAutorId(), pregunta.getAutorNombre(), "autor@unicauca.edu.co",
                    pregunta.getContexto(), todasLasObservaciones, correlationId
            );
            eventPublisher.publish(rechazoEvent);

        } else if ("APROBAR".equals(decision) || "APROBADA".equals(decision)) {
            rev.setEstado(EstadoRevision.APROBADA);
            rev.setFechaDecision(LocalDateTime.now());
            revisionRepository.save(rev);

            // Política de aprobación: Todos los revisores asignados deben haber aprobado
            List<RevisionEntity> todasRevisiones = revisionRepository.findByPreguntaId(preguntaId);
            boolean todosAprobaron = todasRevisiones.stream().allMatch(r -> r.getEstado() == EstadoRevision.APROBADA);

            if (todosAprobaron) {
                lifecycleClient.aplicarDecision(preguntaId, "APROBADA", revisorId, "autor@unicauca.edu.co",
                        pregunta.getContexto(), Collections.emptyList(), correlationId);

                questionsClient.actualizarProyeccionEstado(preguntaId, "APROBADA");

                PreguntaAprobadaEvent aprobadaEvent = new PreguntaAprobadaEvent(
                        preguntaId, pregunta.getAutorId(), pregunta.getAutorNombre(), "autor@unicauca.edu.co",
                        pregunta.getContexto(), "Pregunta aprobada por todos los revisores", correlationId
                );
                eventPublisher.publish(aprobadaEvent);
            }

        } else {
            throw new IllegalArgumentException("Decisión inválida. Valores permitidos: APROBAR o RECHAZAR");
        }

        return rev;
    }

    @Transactional(readOnly = true)
    public List<ObservacionEntity> listarObservaciones(Long preguntaId) {
        return observacionRepository.findByPreguntaIdAndVisibleAutorTrue(preguntaId);
    }
}

