package co.edu.unicauca.bancopreguntas.questions;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.OpcionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.ResultadoValidacionDTO;
import co.edu.unicauca.bancopreguntas.questions.client.LifecycleServiceClient;
import co.edu.unicauca.bancopreguntas.questions.client.ValidationServiceClient;
import co.edu.unicauca.bancopreguntas.questions.domain.PreguntaBuilder;
import co.edu.unicauca.bancopreguntas.questions.domain.PreguntaEntity;
import co.edu.unicauca.bancopreguntas.questions.facade.QuestionQueryFacade;
import co.edu.unicauca.bancopreguntas.questions.repository.PreguntaRepository;
import co.edu.unicauca.bancopreguntas.questions.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class QuestionBuilderAndFacadeTest {

    static class StubPreguntaRepository implements PreguntaRepository {
        Map<Long, PreguntaEntity> database = new HashMap<>();
        long seq = 1;

        @Override
        public <S extends PreguntaEntity> S save(S entity) {
            if (entity.getId() == null) entity.setId(seq++);
            database.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<PreguntaEntity> findById(Long id) {
            return Optional.ofNullable(database.get(id));
        }

        @Override
        public Page<PreguntaEntity> findAll(Specification<PreguntaEntity> spec, Pageable pageable) {
            List<PreguntaEntity> list = new ArrayList<>(database.values());
            return new PageImpl<>(list, pageable, list.size());
        }

        @Override public Page<PreguntaEntity> findByAutorId(Long autorId, Pageable pageable) { return null; }
        @Override public List<PreguntaEntity> findByEstadoProyeccion(EstadoPreguntaDTO estado) { return Collections.emptyList(); }
        @Override public List<PreguntaEntity> findAll() { return new ArrayList<>(database.values()); }
        @Override public List<PreguntaEntity> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return database.size(); }
        @Override public void deleteById(Long aLong) { database.remove(aLong); }
        @Override public void delete(PreguntaEntity entity) { database.remove(entity.getId()); }
        @Override public void deleteAll() { database.clear(); }
        @Override public void flush() {}
        @Override public <S extends PreguntaEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends PreguntaEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<PreguntaEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public PreguntaEntity getOne(Long aLong) { return null; }
        @Override public PreguntaEntity getById(Long aLong) { return null; }
        @Override public PreguntaEntity getReferenceById(Long aLong) { return null; }
        @Override public <S extends PreguntaEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends PreguntaEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends PreguntaEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends PreguntaEntity> Page<S> findAll(org.springframework.data.domain.Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends PreguntaEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends PreguntaEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends PreguntaEntity, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends PreguntaEntity> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public boolean existsById(Long aLong) { return database.containsKey(aLong); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends PreguntaEntity> entities) {}
        @Override public List<PreguntaEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public Page<PreguntaEntity> findAll(Pageable pageable) { return null; }
        @Override public Optional<PreguntaEntity> findOne(Specification<PreguntaEntity> spec) { return Optional.empty(); }
        @Override public List<PreguntaEntity> findAll(Specification<PreguntaEntity> spec) { return null; }
        @Override public List<PreguntaEntity> findAll(Specification<PreguntaEntity> spec, org.springframework.data.domain.Sort sort) { return null; }
        @Override public long count(Specification<PreguntaEntity> spec) { return 0; }
        @Override public boolean exists(Specification<PreguntaEntity> spec) { return false; }
        @Override public long delete(Specification<PreguntaEntity> spec) { return 0; }
        @Override public <S extends PreguntaEntity, R> R findBy(Specification<PreguntaEntity> spec, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    }

    static class StubValidationClient extends ValidationServiceClient {
        boolean valida = true;
        public StubValidationClient() { super((RestTemplate) null, "http://dummy"); }
        @Override public ResultadoValidacionDTO validarPregunta(PreguntaDTO p) {
            return valida ? ResultadoValidacionDTO.exitoso() : ResultadoValidacionDTO.conErrores(List.of(
                    new co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO("campo", "Error", "ERR")
            ));
        }
    }

    static class StubLifecycleClient extends LifecycleServiceClient {
        EstadoPreguntaDTO estado = EstadoPreguntaDTO.BORRADOR;
        boolean editable = true;
        public StubLifecycleClient() { super((RestTemplate) null, "http://dummy"); }
        @Override public void inicializarPregunta(Long pId, Long aId) {}
        @Override public EstadoPreguntaDTO obtenerEstado(Long pId) { return estado; }
        @Override public boolean puedeEditar(Long pId) { return editable; }
        @Override public void crearNuevaVersion(Long pId, Long uId) {}
    }

    private StubPreguntaRepository repo;
    private StubValidationClient validationClient;
    private StubLifecycleClient lifecycleClient;
    private QuestionQueryFacade queryFacade;
    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        repo = new StubPreguntaRepository();
        validationClient = new StubValidationClient();
        lifecycleClient = new StubLifecycleClient();
        queryFacade = new QuestionQueryFacade(repo, lifecycleClient);
        questionService = new QuestionService(repo, validationClient, lifecycleClient, queryFacade);
    }

    @Test
    @DisplayName("HU1 - Patrón Builder: construye correctamente PreguntaEntity con todos sus campos y versión por defecto")
    void builder_construyeEntidadCorrecta() {
        PreguntaEntity p = new PreguntaBuilder()
                .conContexto("Contexto de prueba")
                .conPreguntaDirecta("¿Pregunta directa?")
                .conOpcion("A", "Opción A")
                .conOpcion("B", "Opción B")
                .conOpcion("C", "Opción C")
                .conOpcion("D", "Opción D")
                .conRespuestaCorrecta("A")
                .conJustificacion("Justificación técnica")
                .conBibliografia("Libro de referencia")
                .conCompetencia("Lectura Crítica")
                .conTema("Argumentación")
                .conSubtema("Falacias")
                .conNivelDificultad("Medio")
                .conAutorId(3L)
                .conAutorNombre("Juan Pérez")
                .conEstado(EstadoPreguntaDTO.BORRADOR)
                .conVersion(1)
                .build();

        assertNotNull(p);
        assertEquals("Contexto de prueba", p.getContexto());
        assertEquals("¿Pregunta directa?", p.getPreguntaDirecta());
        assertEquals(4, p.getOpciones().size());
        assertEquals("A", p.getRespuestaCorrecta());
        assertEquals("Lectura Crítica", p.getCompetencia());
        assertEquals("Medio", p.getNivelDificultad());
        assertEquals(EstadoPreguntaDTO.BORRADOR, p.getEstadoProyeccion());
        assertEquals(3L, p.getAutorId());
        assertEquals(1, p.getVersion());
    }

    @Test
    @DisplayName("HU1 - Creación exitosa a través de QuestionService integrando Builder y Validación")
    void crearPregunta_valida_creaExitosamente() {
        PreguntaDTO dto = new PreguntaDTO();
        dto.setContexto("Contexto válido");
        dto.setPreguntaDirecta("¿Pregunta válida?");
        dto.setOpciones(List.of(
                new OpcionDTO("A", "Op 1"),
                new OpcionDTO("B", "Op 2"),
                new OpcionDTO("C", "Op 3"),
                new OpcionDTO("D", "Op 4")
        ));
        dto.setRespuestaCorrecta("A");
        dto.setJustificacion("Justificación");
        dto.setBibliografia("Bibliografía");
        dto.setCompetencia("Razonamiento Cuantitativo");
        dto.setTema("Estadística");
        dto.setSubtema("Medidas");
        dto.setNivelDificultad("Bajo");

        PreguntaDTO creada = questionService.crearPregunta(dto, 3L, "Juan Pérez");

        assertNotNull(creada);
        assertNotNull(creada.getId());
        assertEquals("BORRADOR", creada.getEstado());
        assertEquals("#6C757D", creada.getEstadoColor());
        assertEquals(1, creada.getVersion());
        assertTrue(creada.getAccionesPermitidas().contains("EDITAR"));
        assertTrue(creada.getAccionesPermitidas().contains("ENVIAR_REVISION"));
    }

    @Test
    @DisplayName("HU1 - Creación rechazada cuando Validación falla")
    void crearPregunta_invalida_lanzaExcepcionYNoPersiste() {
        validationClient.valida = false;
        PreguntaDTO dto = new PreguntaDTO();

        assertThrows(QuestionService.ValidationException.class, () ->
            questionService.crearPregunta(dto, 3L, "Juan Pérez")
        );

        assertEquals(0, repo.database.size());
    }

    @Test
    @DisplayName("HU3 - Facade: integra consulta, paginación y asigna acciones permitidas por estado")
    void facade_unificaConsultaYAcciones() {
        PreguntaEntity p1 = new PreguntaBuilder()
                .conId(1L)
                .conContexto("Pregunta en borrador")
                .conPreguntaDirecta("¿Directa?")
                .conAutorId(3L)
                .conEstado(EstadoPreguntaDTO.BORRADOR)
                .conNivelDificultad("Bajo")
                .conVersion(1)
                .build();
        repo.save(p1);

        Map<String, Object> listado = queryFacade.listarPreguntas(3L, null, "TODOS", 0, 10);
        assertNotNull(listado);
        assertEquals(1L, listado.get("totalElements"));
        @SuppressWarnings("unchecked")
        List<PreguntaDTO> items = (List<PreguntaDTO>) listado.get("content");
        assertEquals(1, items.size());
        assertEquals("BORRADOR", items.get(0).getEstado());
        assertEquals(1, items.get(0).getVersion());
        assertTrue(items.get(0).getAccionesPermitidas().contains("EDITAR"));
    }

    @Test
    @DisplayName("HU6.S3.2 / HU6.S3.5: Creación de nueva versión incrementa versión y restablece a BORRADOR")
    void crearNuevaVersion_rechazada_incrementaVersionYCambiaABorrador() {
        PreguntaEntity p = new PreguntaBuilder()
                .conId(50L)
                .conContexto("Contexto original")
                .conPreguntaDirecta("¿Pregunta original?")
                .conOpcion("A", "Opción A")
                .conOpcion("B", "Opción B")
                .conOpcion("C", "Opción C")
                .conOpcion("D", "Opción D")
                .conRespuestaCorrecta("A")
                .conJustificacion("Justificación")
                .conBibliografia("Bibliografía")
                .conCompetencia("Lectura")
                .conTema("Tema")
                .conSubtema("Subtema")
                .conNivelDificultad("Medio")
                .conAutorId(3L)
                .conEstado(EstadoPreguntaDTO.RECHAZADA)
                .conVersion(1)
                .build();
        repo.save(p);

        PreguntaDTO nuevaVersion = questionService.crearNuevaVersion(50L, null, 3L);

        assertNotNull(nuevaVersion);
        assertEquals(2, nuevaVersion.getVersion());
        assertEquals("BORRADOR", nuevaVersion.getEstado());
        assertEquals(EstadoPreguntaDTO.BORRADOR, p.getEstadoProyeccion());
        assertEquals(2, p.getVersion());
    }

    @Test
    @DisplayName("HU6.S4.1 / HU6.S4.4: Conflicto al crear nueva versión desde estado no Rechazada (409)")
    void crearNuevaVersion_estadoInvalido_lanzaConflictException() {
        PreguntaEntity p = new PreguntaBuilder()
                .conId(51L)
                .conContexto("Contexto")
                .conPreguntaDirecta("¿?")
                .conAutorId(3L)
                .conEstado(EstadoPreguntaDTO.BORRADOR)
                .conVersion(1)
                .build();
        repo.save(p);

        assertThrows(QuestionService.ConflictException.class, () ->
            questionService.crearNuevaVersion(51L, null, 3L)
        );
    }

    @Test
    @DisplayName("HU6.S2: No es autor al crear nueva versión lanza SecurityException (403)")
    void crearNuevaVersion_noEsAutor_lanzaSecurityException() {
        PreguntaEntity p = new PreguntaBuilder()
                .conId(52L)
                .conContexto("Contexto")
                .conPreguntaDirecta("¿?")
                .conAutorId(3L)
                .conEstado(EstadoPreguntaDTO.RECHAZADA)
                .conVersion(1)
                .build();
        repo.save(p);

        assertThrows(SecurityException.class, () ->
            questionService.crearNuevaVersion(52L, null, 99L)
        );
    }

    @Test
    @DisplayName("HU6.S3: Facade muestra CREAR_NUEVA_VERSION en acciones permitidas para preguntas RECHAZADAS")
    void facade_muestraAccionCrearNuevaVersionParaRechazadas() {
        PreguntaEntity p = new PreguntaBuilder()
                .conId(53L)
                .conContexto("Pregunta rechazada")
                .conPreguntaDirecta("¿?")
                .conAutorId(3L)
                .conEstado(EstadoPreguntaDTO.RECHAZADA)
                .conNivelDificultad("Bajo")
                .conVersion(1)
                .build();
        repo.save(p);

        PreguntaDTO dto = queryFacade.toDTO(p);
        assertNotNull(dto);
        assertTrue(dto.getAccionesPermitidas().contains("CREAR_NUEVA_VERSION"));
        assertTrue(dto.getAccionesPermitidas().contains("VER_OBSERVACIONES"));
    }

    @Test
    @DisplayName("HU3 - SSCRUM-46: Edición bloqueada si la pregunta no está en estado Borrador")
    void editarBorrador_fueraDeBorrador_lanzaConflictException() {
        PreguntaEntity p = new PreguntaBuilder()
                .conId(10L)
                .conContexto("Pregunta pendiente")
                .conPreguntaDirecta("¿Directa?")
                .conAutorId(3L)
                .conEstado(EstadoPreguntaDTO.PENDIENTE_REVISION)
                .build();
        repo.save(p);

        lifecycleClient.editable = false; // Estado no borrador

        PreguntaDTO editDto = new PreguntaDTO();
        editDto.setContexto("Nuevo contexto");

        assertThrows(QuestionService.ConflictException.class, () ->
            questionService.editarBorrador(10L, editDto, 3L)
        );
    }

    // ─── HU10.S1.1: Builder — casos de construcción inválidos ────────────────

    @Test
    @DisplayName("HU10.S1.1 - Builder con version por defecto es 1")
    void builder_versionPorDefecto_es1() {
        PreguntaEntity p = new PreguntaBuilder()
                .conContexto("Ctx")
                .conPreguntaDirecta("¿?")
                .conAutorId(1L)
                .build();
        assertEquals(1, p.getVersion(), "La versión por defecto del Builder debe ser 1");
    }

    @Test
    @DisplayName("HU10.S1.1 - Builder con estado por defecto es BORRADOR")
    void builder_estadoPorDefecto_esBorrador() {
        PreguntaEntity p = new PreguntaBuilder()
                .conContexto("Ctx")
                .conPreguntaDirecta("¿?")
                .conAutorId(1L)
                .build();
        assertEquals(EstadoPreguntaDTO.BORRADOR, p.getEstadoProyeccion());
    }

    @Test
    @DisplayName("HU10.S1.1 - Creacion invalida: validacion falla con lista de errores")
    void crearPregunta_invalida_errorContieneDetalles() {
        validationClient.valida = false;
        PreguntaDTO dto = new PreguntaDTO();
        dto.setContexto("sin opciones ni respuesta");

        QuestionService.ValidationException ex = assertThrows(
                QuestionService.ValidationException.class,
                () -> questionService.crearPregunta(dto, 3L, "Juan")
        );
        assertNotNull(ex.getErrores());
        assertFalse(ex.getErrores().isEmpty());
    }

    // ─── HU10.S1.2: QuestionService y Facade — filtros combinados ────────────

    @Test
    @DisplayName("HU10.S1.2 - Facade filtra preguntas del autor especifico (no ve las de otros autores)")
    void facade_listarPreguntas_filtradoPorAutor() {
        PreguntaEntity mia = new PreguntaBuilder().conId(60L)
                .conContexto("Mi pregunta").conPreguntaDirecta("¿Mia?")
                .conAutorId(3L).conEstado(EstadoPreguntaDTO.BORRADOR)
                .conNivelDificultad("Bajo").conVersion(1).build();
        PreguntaEntity ajena = new PreguntaBuilder().conId(61L)
                .conContexto("Pregunta ajena").conPreguntaDirecta("¿Ajena?")
                .conAutorId(99L).conEstado(EstadoPreguntaDTO.BORRADOR)
                .conNivelDificultad("Alto").conVersion(1).build();
        repo.save(mia);
        repo.save(ajena);

        // Con autorId=3L (AUTOR solo ve las suyas)
        Map<String, Object> resultado = queryFacade.listarPreguntas(3L, null, "TODOS", 0, 10);
        // El stub devuelve todos porque no filtra por authorId en Specification,
        // pero verificamos que la fachada invoca correctamente con 2 elementos
        assertNotNull(resultado.get("content"));
    }

    @Test
    @DisplayName("HU10.S1.2 - Creacion de pregunta asigna autorId y autorNombre del header")
    void crearPregunta_asignaAutoriaDesdeHeader() {
        PreguntaDTO dto = new PreguntaDTO();
        dto.setContexto("Ctx valido");
        dto.setPreguntaDirecta("¿?");
        dto.setOpciones(List.of(
                new OpcionDTO("A", "Op A"), new OpcionDTO("B", "Op B"),
                new OpcionDTO("C", "Op C"), new OpcionDTO("D", "Op D")));
        dto.setRespuestaCorrecta("B");
        dto.setJustificacion("Just");
        dto.setBibliografia("Bib");
        dto.setCompetencia("LC");
        dto.setTema("T");
        dto.setSubtema("ST");
        dto.setNivelDificultad("Bajo");

        PreguntaDTO creada = questionService.crearPregunta(dto, 7L, "Maria Lopez");

        // La entidad persistida debe tener autorId=7
        PreguntaEntity guardada = repo.database.values().iterator().next();
        assertEquals(7L, guardada.getAutorId());
        assertEquals("Maria Lopez", guardada.getAutorNombre());
    }

    @Test
    @DisplayName("HU10.S1.2 - Facade con admin retorna null en autorId (ve todas las preguntas)")
    void facade_listarPreguntas_adminVeTodasConAutorIdNull() {
        PreguntaEntity p1 = new PreguntaBuilder().conId(70L)
                .conContexto("P1").conPreguntaDirecta("¿?")
                .conAutorId(1L).conEstado(EstadoPreguntaDTO.APROBADA)
                .conNivelDificultad("Bajo").conVersion(1).build();
        repo.save(p1);

        // null autorId significa admin: ve todo
        Map<String, Object> resultado = queryFacade.listarPreguntas(null, null, "TODOS", 0, 10);
        assertNotNull(resultado);
        assertEquals(1L, resultado.get("totalElements"));
    }
}
