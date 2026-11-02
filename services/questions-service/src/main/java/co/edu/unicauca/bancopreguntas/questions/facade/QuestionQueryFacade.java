package co.edu.unicauca.bancopreguntas.questions.facade;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.OpcionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.questions.client.LifecycleServiceClient;
import co.edu.unicauca.bancopreguntas.questions.domain.OpcionEntity;
import co.edu.unicauca.bancopreguntas.questions.domain.PreguntaEntity;
import co.edu.unicauca.bancopreguntas.questions.repository.PreguntaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Patrón Facade (GoF Structural Pattern) para unificar la consulta de preguntas
 * y el estado autoritativo del ciclo de vida en una respuesta integrada (HU3).
 * Mantiene los límites del contexto delimitado mediante proyección local y sincronización.
 */
@Component
public class QuestionQueryFacade {

    private final PreguntaRepository preguntaRepository;
    private final LifecycleServiceClient lifecycleClient;

    public QuestionQueryFacade(PreguntaRepository preguntaRepository, LifecycleServiceClient lifecycleClient) {
        this.preguntaRepository = Objects.requireNonNull(preguntaRepository, "preguntaRepository es obligatorio");
        this.lifecycleClient = Objects.requireNonNull(lifecycleClient, "lifecycleClient es obligatorio");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listarPreguntas(Long autorId, List<EstadoPreguntaDTO> estados, 
                                               String dificultad, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "id"));

        Specification<PreguntaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (autorId != null) {
                predicates.add(cb.equal(root.get("autorId"), autorId));
            }

            if (estados != null && !estados.isEmpty()) {
                predicates.add(root.get("estadoProyeccion").in(estados));
            }

            if (dificultad != null && !dificultad.isBlank() && !"TODOS".equalsIgnoreCase(dificultad.trim())) {
                predicates.add(cb.equal(cb.upper(root.get("nivelDificultad")), dificultad.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<PreguntaEntity> resultPage = preguntaRepository.findAll(spec, pageable);

        List<PreguntaDTO> dtoList = resultPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", dtoList);
        response.put("currentPage", resultPage.getNumber());
        response.put("totalElements", resultPage.getTotalElements());
        response.put("totalPages", resultPage.getTotalPages());
        response.put("size", resultPage.getSize());
        response.put("numberOfElements", resultPage.getNumberOfElements());

        return response;
    }

    @Transactional(readOnly = true)
    public Optional<PreguntaDTO> consultarDetalle(Long preguntaId, Long solicitanteId, boolean esAdmin) {
        return preguntaRepository.findById(preguntaId).map(entity -> {
            // Verificar autoría salvo administrador
            if (!esAdmin && solicitanteId != null && !solicitanteId.equals(entity.getAutorId())) {
                throw new SecurityException("No tiene permisos para ver esta pregunta");
            }

            PreguntaDTO dto = toDTO(entity);

            // Sincronizar estado autoritativo en detalle
            try {
                EstadoPreguntaDTO estadoAutoritativo = lifecycleClient.obtenerEstado(preguntaId);
                if (estadoAutoritativo != null) {
                    dto.setEstado(estadoAutoritativo.name());
                    dto.setEstadoColor(estadoAutoritativo.getColorHex());
                    actualizarAccionesPermitidas(dto, estadoAutoritativo);
                }
            } catch (Exception e) {
                // Conservar estado de proyección si el servicio externo no responde
            }

            return dto;
        });
    }

    public PreguntaDTO toDTO(PreguntaEntity entity) {
        PreguntaDTO dto = new PreguntaDTO();
        dto.setId(entity.getId());
        dto.setContexto(entity.getContexto());
        dto.setPreguntaDirecta(entity.getPreguntaDirecta());
        dto.setRespuestaCorrecta(entity.getRespuestaCorrecta());
        dto.setJustificacion(entity.getJustificacion());
        dto.setBibliografia(entity.getBibliografia());
        dto.setCompetencia(entity.getCompetencia());
        dto.setTema(entity.getTema());
        dto.setSubtema(entity.getSubtema());
        dto.setNivelDificultad(entity.getNivelDificultad());
        dto.setAutorId(entity.getAutorId());
        dto.setAutorNombre(entity.getAutorNombre());
        dto.setFechaCreacion(entity.getFechaCreacion());

        EstadoPreguntaDTO estado = entity.getEstadoProyeccion() != null ? entity.getEstadoProyeccion() : EstadoPreguntaDTO.BORRADOR;
        dto.setEstado(estado.name());
        dto.setEstadoColor(estado.getColorHex());

        if (entity.getOpciones() != null) {
            List<OpcionDTO> opcionesDTO = entity.getOpciones().stream()
                    .map(op -> new OpcionDTO(op.getClave(), op.getTexto()))
                    .collect(Collectors.toList());
            dto.setOpciones(opcionesDTO);
        }

        actualizarAccionesPermitidas(dto, estado);
        return dto;
    }

    private void actualizarAccionesPermitidas(PreguntaDTO dto, EstadoPreguntaDTO estado) {
        List<String> acciones = new ArrayList<>();
        acciones.add("VER_DETALLE");

        if (estado == EstadoPreguntaDTO.BORRADOR) {
            acciones.add("EDITAR");
            acciones.add("ENVIAR_REVISION");
        } else if (estado == EstadoPreguntaDTO.RECHAZADA) {
            acciones.add("VER_OBSERVACIONES");
        }

        dto.setAccionesPermitidas(acciones);
    }
}

