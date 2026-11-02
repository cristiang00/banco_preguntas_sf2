package co.edu.unicauca.bancopreguntas.questions.service;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.OpcionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.ResultadoValidacionDTO;
import co.edu.unicauca.bancopreguntas.questions.client.LifecycleServiceClient;
import co.edu.unicauca.bancopreguntas.questions.client.ValidationServiceClient;
import co.edu.unicauca.bancopreguntas.questions.domain.OpcionEntity;
import co.edu.unicauca.bancopreguntas.questions.domain.PreguntaBuilder;
import co.edu.unicauca.bancopreguntas.questions.domain.PreguntaEntity;
import co.edu.unicauca.bancopreguntas.questions.facade.QuestionQueryFacade;
import co.edu.unicauca.bancopreguntas.questions.repository.PreguntaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    private final PreguntaRepository preguntaRepository;
    private final ValidationServiceClient validationClient;
    private final LifecycleServiceClient lifecycleClient;
    private final QuestionQueryFacade queryFacade;

    public static class ValidationException extends RuntimeException {
        private final List<ErrorValidacionDTO> errores;
        public ValidationException(List<ErrorValidacionDTO> errores) {
            super("Error de validación estructural");
            this.errores = errores;
        }
        public List<ErrorValidacionDTO> getErrores() { return errores; }
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }

    public QuestionService(PreguntaRepository preguntaRepository,
                           ValidationServiceClient validationClient,
                           LifecycleServiceClient lifecycleClient,
                           QuestionQueryFacade queryFacade) {
        this.preguntaRepository = preguntaRepository;
        this.validationClient = validationClient;
        this.lifecycleClient = lifecycleClient;
        this.queryFacade = queryFacade;
    }

    @Transactional
    public PreguntaDTO crearPregunta(PreguntaDTO dto, Long autorId, String autorNombre) {
        // 1. Invocar microservicio de Validación estructural (HU1 - SSCRUM-34 a SSCRUM-40)
        dto.setAutorId(autorId);
        dto.setAutorNombre(autorNombre);
        ResultadoValidacionDTO valResult = validationClient.validarPregunta(dto);
        if (!valResult.isValido()) {
            throw new ValidationException(valResult.getErrores());
        }

        // 2. Construir la entidad mediante el patrón Builder (GoF)
        PreguntaEntity entity = new PreguntaBuilder()
                .conContexto(dto.getContexto())
                .conPreguntaDirecta(dto.getPreguntaDirecta())
                .conOpciones(dto.getOpciones())
                .conRespuestaCorrecta(dto.getRespuestaCorrecta())
                .conJustificacion(dto.getJustificacion())
                .conBibliografia(dto.getBibliografia())
                .conCompetencia(dto.getCompetencia())
                .conTema(dto.getTema())
                .conSubtema(dto.getSubtema())
                .conNivelDificultad(dto.getNivelDificultad())
                .conAutorId(autorId)
                .conAutorNombre(autorNombre)
                .conEstado(EstadoPreguntaDTO.BORRADOR)
                .conFechaCreacion(LocalDateTime.now())
                .build();

        // 3. Persistir entidad
        PreguntaEntity guardada = preguntaRepository.save(entity);

        // 4. Inicializar en Lifecycle Service
        lifecycleClient.inicializarPregunta(guardada.getId(), autorId);

        return queryFacade.toDTO(guardada);
    }

    @Transactional
    public PreguntaDTO editarBorrador(Long id, PreguntaDTO dto, Long solicitanteId) {
        PreguntaEntity entity = preguntaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La pregunta con ID " + id + " no existe"));

        if (!entity.getAutorId().equals(solicitanteId)) {
            throw new SecurityException("No tiene permisos para editar esta pregunta");
        }

        // HU2 - SSCRUM-46: Consultar si puede editar en Lifecycle Service
        boolean editable = lifecycleClient.puedeEditar(id);
        if (!editable) {
            throw new ConflictException("Solo se pueden editar preguntas en estado Borrador");
        }

        // Validar campos modificados
        dto.setId(id);
        ResultadoValidacionDTO valResult = validationClient.validarPregunta(dto);
        if (!valResult.isValido()) {
            throw new ValidationException(valResult.getErrores());
        }

        entity.setContexto(dto.getContexto());
        entity.setPreguntaDirecta(dto.getPreguntaDirecta());
        entity.setRespuestaCorrecta(dto.getRespuestaCorrecta());
        entity.setJustificacion(dto.getJustificacion());
        entity.setBibliografia(dto.getBibliografia());
        entity.setCompetencia(dto.getCompetencia());
        entity.setTema(dto.getTema());
        entity.setSubtema(dto.getSubtema());
        entity.setNivelDificultad(dto.getNivelDificultad());

        if (dto.getOpciones() != null && dto.getOpciones().size() == 4) {
            entity.getOpciones().clear();
            int orden = 1;
            for (OpcionDTO op : dto.getOpciones()) {
                entity.getOpciones().add(new OpcionEntity(op.getClave(), op.getTexto(), orden++));
            }
        }

        PreguntaEntity actualizada = preguntaRepository.save(entity);
        return queryFacade.toDTO(actualizada);
    }

    @Transactional
    public void actualizarProyeccionEstado(Long preguntaId, EstadoPreguntaDTO nuevoEstado) {
        preguntaRepository.findById(preguntaId).ifPresent(p -> {
            p.setEstadoProyeccion(nuevoEstado);
            preguntaRepository.save(p);
        });
    }

    @Transactional(readOnly = true)
    public List<PreguntaDTO> listarPendientesDeRevision() {
        return preguntaRepository.findByEstadoProyeccion(EstadoPreguntaDTO.PENDIENTE_REVISION)
                .stream()
                .map(queryFacade::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }
}

