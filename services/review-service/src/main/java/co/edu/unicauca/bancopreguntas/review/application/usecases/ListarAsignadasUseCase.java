package co.edu.unicauca.bancopreguntas.review.application.usecases;

import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.PreguntasClientPort;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.RevisionRepositoryPort;
import co.edu.unicauca.bancopreguntas.review.domain.model.EstadoRevision;
import co.edu.unicauca.bancopreguntas.review.domain.model.Revision;

import java.util.*;

/**
 * Caso de uso: Listar preguntas asignadas con filtros y paginación en BD (HU5 / HU12 / HU8).
 */
public class ListarAsignadasUseCase {

    private final RevisionRepositoryPort repositoryPort;
    private final PreguntasClientPort preguntasClientPort;

    public ListarAsignadasUseCase(RevisionRepositoryPort repositoryPort, PreguntasClientPort preguntasClientPort) {
        this.repositoryPort = repositoryPort;
        this.preguntasClientPort = preguntasClientPort;
    }

    public Map<String, Object> execute(Long revisorId, int page, int size, String estadoStr,
                                       String dificultad, String competencia) {
        EstadoRevision estado = null;
        if (estadoStr != null && !estadoStr.isBlank() && !"TODOS".equalsIgnoreCase(estadoStr)) {
            try {
                estado = EstadoRevision.valueOf(estadoStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        String difParam = (dificultad != null && !dificultad.isBlank() && !"TODOS".equalsIgnoreCase(dificultad))
                ? dificultad.trim() : null;
        String compParam = (competencia != null && !competencia.isBlank() && !"TODOS".equalsIgnoreCase(competencia))
                ? competencia.trim() : null;

        Map<String, Object> pageData = repositoryPort.findByRevisorIdWithFilters(
                revisorId, estado, compParam, difParam, page, size
        );

        @SuppressWarnings("unchecked")
        List<Revision> revisiones = (List<Revision>) pageData.get("content");
        List<Map<String, Object>> resultList = new ArrayList<>();

        if (revisiones != null) {
            for (Revision rev : revisiones) {
                Optional<PreguntaDTO> pOpt = preguntasClientPort.obtenerPregunta(rev.getPreguntaId());
                if (pOpt.isPresent()) {
                    PreguntaDTO p = pOpt.get();
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("revisionId", rev.getId());
                    item.put("preguntaId", p.getId());
                    item.put("contexto", p.getContexto());
                    item.put("preguntaDirecta", p.getPreguntaDirecta());
                    item.put("competencia", p.getCompetencia() != null ? p.getCompetencia() : rev.getCompetencia());
                    item.put("tema", p.getTema());
                    item.put("subtema", p.getSubtema());
                    item.put("nivelDificultad", p.getNivelDificultad() != null ? p.getNivelDificultad() : rev.getNivelDificultad());
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
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", resultList);
        response.put("currentPage", pageData.get("currentPage"));
        response.put("totalElements", pageData.get("totalElements"));
        response.put("totalPages", pageData.get("totalPages"));
        return response;
    }
}

