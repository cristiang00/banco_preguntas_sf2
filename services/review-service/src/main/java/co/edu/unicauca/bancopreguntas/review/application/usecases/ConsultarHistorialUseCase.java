package co.edu.unicauca.bancopreguntas.review.application.usecases;

import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.PreguntasClientPort;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.RevisionRepositoryPort;
import co.edu.unicauca.bancopreguntas.review.domain.model.Observacion;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Caso de uso: Consultar historial de revisiones y observaciones por pregunta (HU6 / HU8).
 * Valida que únicamente el autor de la pregunta tenga permisos de acceso (403 Forbidden en caso contrario).
 */
public class ConsultarHistorialUseCase {

    private final RevisionRepositoryPort repositoryPort;
    private final PreguntasClientPort preguntasClientPort;

    public ConsultarHistorialUseCase(RevisionRepositoryPort repositoryPort, PreguntasClientPort preguntasClientPort) {
        this.repositoryPort = repositoryPort;
        this.preguntasClientPort = preguntasClientPort;
    }

    public Map<String, Object> execute(Long preguntaId, Long usuarioAutenticadoId, int page, int size) {
        PreguntaDTO pregunta = preguntasClientPort.obtenerPregunta(preguntaId)
                .orElseThrow(() -> new IllegalArgumentException("La pregunta con ID " + preguntaId + " no existe"));

        // HU6 - Subtarea 2: Validación estricta de autoría
        if (pregunta.getAutorId() != null && usuarioAutenticadoId != null
                && !pregunta.getAutorId().equals(usuarioAutenticadoId)) {
            throw new SecurityException("Acceso denegado: solo el autor de la pregunta puede consultar su historial de revisiones");
        }

        List<Revision> todasRevisiones = repositoryPort.findByPreguntaId(preguntaId);
        List<Observacion> todasObservaciones = repositoryPort.findObservacionesByPreguntaId(preguntaId);

        Map<Long, List<Observacion>> obsPorRevision = todasObservaciones.stream()
                .filter(Observacion::isVisibleAutor)
                .collect(Collectors.groupingBy(Observacion::getRevisionId));

        List<Map<String, Object>> rondas = new ArrayList<>();
        for (Revision rev : todasRevisiones) {
            Map<String, Object> ronda = new LinkedHashMap<>();
            ronda.put("revisionId", rev.getId());
            ronda.put("revisorId", rev.getRevisorId());
            ronda.put("revisorNombre", rev.getRevisorNombre());
            ronda.put("estado", rev.getEstado().name());
            ronda.put("fechaAsignacion", rev.getFechaAsignacion());
            ronda.put("fechaDecision", rev.getFechaDecision());

            List<Observacion> obs = obsPorRevision.getOrDefault(rev.getId(), Collections.emptyList());
            List<String> textoObs = obs.stream().map(Observacion::getTexto).collect(Collectors.toList());
            ronda.put("observaciones", textoObs);

            rondas.add(ronda);
        }

        // Ordenar por fecha cronológica inversa (más reciente primero)
        rondas.sort((a, b) -> {
            java.time.LocalDateTime f1 = (java.time.LocalDateTime) a.get("fechaAsignacion");
            java.time.LocalDateTime f2 = (java.time.LocalDateTime) b.get("fechaAsignacion");
            if (f1 == null || f2 == null) return 0;
            return f2.compareTo(f1);
        });

        // Paginación manual de la lista agrupada
        int pageSize = Math.max(1, size);
        int pageIndex = Math.max(0, page);
        int totalElements = rondas.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        int start = pageIndex * pageSize;
        int end = Math.min(start + pageSize, totalElements);
        List<Map<String, Object>> content = (start < totalElements) ? rondas.subList(start, end) : Collections.emptyList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("preguntaId", preguntaId);
        response.put("autorId", pregunta.getAutorId());
        response.put("content", content);
        response.put("currentPage", pageIndex);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        return response;
    }
}

