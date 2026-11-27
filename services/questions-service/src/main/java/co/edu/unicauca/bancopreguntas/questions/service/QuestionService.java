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
import co.edu.unicauca.bancopreguntas.questions.domain.PreguntaVersionEntity;
import co.edu.unicauca.bancopreguntas.questions.facade.QuestionQueryFacade;
import co.edu.unicauca.bancopreguntas.questions.repository.PreguntaRepository;
import co.edu.unicauca.bancopreguntas.questions.repository.PreguntaVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final PreguntaRepository preguntaRepository;
    private final ValidationServiceClient validationClient;
    private final LifecycleServiceClient lifecycleClient;
    private final QuestionQueryFacade queryFacade;
    private final PreguntaVersionRepository preguntaVersionRepository;

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
        this(preguntaRepository, validationClient, lifecycleClient, queryFacade, null);
    }

    @Autowired
    public QuestionService(PreguntaRepository preguntaRepository,
                           ValidationServiceClient validationClient,
                           LifecycleServiceClient lifecycleClient,
                           QuestionQueryFacade queryFacade,
                           @Autowired(required = false) PreguntaVersionRepository preguntaVersionRepository) {
        this.preguntaRepository = preguntaRepository;
        this.validationClient = validationClient;
        this.lifecycleClient = lifecycleClient;
        this.queryFacade = queryFacade;
        this.preguntaVersionRepository = preguntaVersionRepository;
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
                .conVersion(1)
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
    public PreguntaDTO crearNuevaVersion(Long id, PreguntaDTO dto, Long userId) {
        PreguntaEntity entity = preguntaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La pregunta con ID " + id + " no existe"));

        // HU6.S2 / HU6.S3: Validar autoría
        if (!entity.getAutorId().equals(userId)) {
            throw new SecurityException("No tiene permisos para crear una nueva versión de esta pregunta");
        }

        // HU6.S4: Validar estado actual (solo permitido desde Rechazada)
        if (entity.getEstadoProyeccion() != EstadoPreguntaDTO.RECHAZADA) {
            throw new ConflictException("No se puede crear una nueva versión desde el estado '"
                    + entity.getEstadoProyeccion().name() + "'. Solo está permitido desde estado 'RECHAZADA'");
        }

        // HU6.S3.2: Construir DTO efectivo y aplicar siempre validación estructural de la HU1
        PreguntaDTO dtoEfectivo = new PreguntaDTO();
        dtoEfectivo.setId(id);
        dtoEfectivo.setContexto((dto != null && dto.getContexto() != null) ? dto.getContexto() : entity.getContexto());
        dtoEfectivo.setPreguntaDirecta((dto != null && dto.getPreguntaDirecta() != null) ? dto.getPreguntaDirecta() : entity.getPreguntaDirecta());
        dtoEfectivo.setRespuestaCorrecta((dto != null && dto.getRespuestaCorrecta() != null) ? dto.getRespuestaCorrecta() : entity.getRespuestaCorrecta());
        dtoEfectivo.setJustificacion((dto != null && dto.getJustificacion() != null) ? dto.getJustificacion() : entity.getJustificacion());
        dtoEfectivo.setBibliografia((dto != null && dto.getBibliografia() != null) ? dto.getBibliografia() : entity.getBibliografia());
        dtoEfectivo.setCompetencia((dto != null && dto.getCompetencia() != null) ? dto.getCompetencia() : entity.getCompetencia());
        dtoEfectivo.setTema((dto != null && dto.getTema() != null) ? dto.getTema() : entity.getTema());
        dtoEfectivo.setSubtema((dto != null && dto.getSubtema() != null) ? dto.getSubtema() : entity.getSubtema());
        dtoEfectivo.setNivelDificultad((dto != null && dto.getNivelDificultad() != null) ? dto.getNivelDificultad() : entity.getNivelDificultad());
        if (dto != null && dto.getOpciones() != null && !dto.getOpciones().isEmpty()) {
            dtoEfectivo.setOpciones(dto.getOpciones());
        } else if (entity.getOpciones() != null) {
            dtoEfectivo.setOpciones(entity.getOpciones().stream()
                    .map(op -> new OpcionDTO(op.getClave(), op.getTexto()))
                    .collect(Collectors.toList()));
        }

        ResultadoValidacionDTO valResult = validationClient.validarPregunta(dtoEfectivo);
        if (!valResult.isValido()) {
            throw new ValidationException(valResult.getErrores());
        }

        // Notificar a Lifecycle Service la transición Rechazada -> Borrador antes de mutar la entidad
        lifecycleClient.crearNuevaVersion(id, userId);

        // HU6.S3: Conservar snapshot inmutable de la versión anterior y su historial
        if (preguntaVersionRepository != null) {
            preguntaVersionRepository.save(PreguntaVersionEntity.fromPregunta(entity));
        }

        // Aplicar modificaciones sobre la nueva versión
        entity.setContexto(dtoEfectivo.getContexto());
        entity.setPreguntaDirecta(dtoEfectivo.getPreguntaDirecta());
        entity.setRespuestaCorrecta(dtoEfectivo.getRespuestaCorrecta());
        entity.setJustificacion(dtoEfectivo.getJustificacion());
        entity.setBibliografia(dtoEfectivo.getBibliografia());
        entity.setCompetencia(dtoEfectivo.getCompetencia());
        entity.setTema(dtoEfectivo.getTema());
        entity.setSubtema(dtoEfectivo.getSubtema());
        entity.setNivelDificultad(dtoEfectivo.getNivelDificultad());

        if (dto != null && dto.getOpciones() != null && dto.getOpciones().size() == 4) {
            entity.getOpciones().clear();
            int orden = 1;
            for (OpcionDTO op : dto.getOpciones()) {
                entity.getOpciones().add(new OpcionEntity(op.getClave(), op.getTexto(), orden++));
            }
        }

        // HU6.S3: Incrementar versión y restablecer estado a Borrador
        int nuevaVersion = (entity.getVersion() != null ? entity.getVersion() : 1) + 1;
        entity.setVersion(nuevaVersion);
        entity.setEstadoProyeccion(EstadoPreguntaDTO.BORRADOR);

        PreguntaEntity versionada = preguntaRepository.save(entity);
        return queryFacade.toDTO(versionada);
    }

    @Transactional(readOnly = true)
    public List<PreguntaVersionEntity> listarVersionesAnteriores(Long preguntaId) {
        if (preguntaVersionRepository == null) {
            return Collections.emptyList();
        }
        return preguntaVersionRepository.findByPreguntaIdOrderByVersionAsc(preguntaId);
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
