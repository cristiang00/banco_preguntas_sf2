package co.edu.unicauca.bancopreguntas.validation;

import co.edu.unicauca.bancopreguntas.contracts.dto.ErrorValidacionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.OpcionDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.ResultadoValidacionDTO;
import co.edu.unicauca.bancopreguntas.validation.client.CatalogClient;
import co.edu.unicauca.bancopreguntas.validation.domain.ResultadoValidacionEntity;
import co.edu.unicauca.bancopreguntas.validation.repository.ResultadoValidacionRepository;
import co.edu.unicauca.bancopreguntas.validation.rules.*;
import co.edu.unicauca.bancopreguntas.validation.service.ValidationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ValidationRulesTest {

    private StubCatalogClient catalogClient;
    private ReglaCamposObligatorios reglaCampos;
    private ReglaRespuestaCorrecta reglaRespuesta;
    private ReglaDificultad reglaDificultad;
    private ReglaBibliografia reglaBibliografia;
    private ReglaCatalogos reglaCatalogos;

    static class StubCatalogClient implements CatalogClient {
        boolean competenciaValida = true;
        boolean temaValido = true;
        boolean subtemaValido = true;

        @Override public boolean esCompetenciaValida(String c) { return competenciaValida; }
        @Override public boolean esTemaValido(String t) { return temaValido; }
        @Override public boolean esSubtemaValido(String s) { return subtemaValido; }
    }

    static class InMemoryResultadoRepository implements ResultadoValidacionRepository {
        List<ResultadoValidacionEntity> saved = new ArrayList<>();
        @Override public <S extends ResultadoValidacionEntity> S save(S entity) { saved.add(entity); return entity; }
        @Override public <S extends ResultadoValidacionEntity> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<ResultadoValidacionEntity> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<ResultadoValidacionEntity> findAll() { return saved; }
        @Override public List<ResultadoValidacionEntity> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return saved.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(ResultadoValidacionEntity entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends ResultadoValidacionEntity> entities) {}
        @Override public void deleteAll() {}
        @Override public void flush() {}
        @Override public <S extends ResultadoValidacionEntity> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ResultadoValidacionEntity> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<ResultadoValidacionEntity> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public ResultadoValidacionEntity getOne(Long aLong) { return null; }
        @Override public ResultadoValidacionEntity getById(Long aLong) { return null; }
        @Override public ResultadoValidacionEntity getReferenceById(Long aLong) { return null; }
        @Override public <S extends ResultadoValidacionEntity> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends ResultadoValidacionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends ResultadoValidacionEntity> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends ResultadoValidacionEntity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends ResultadoValidacionEntity> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends ResultadoValidacionEntity> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends ResultadoValidacionEntity, R> R findBy(org.springframework.data.domain.Example<S> example, Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<ResultadoValidacionEntity> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public org.springframework.data.domain.Page<ResultadoValidacionEntity> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
    }

    @BeforeEach
    void setUp() {
        catalogClient = new StubCatalogClient();
        reglaCampos = new ReglaCamposObligatorios();
        reglaRespuesta = new ReglaRespuestaCorrecta();
        reglaDificultad = new ReglaDificultad();
        reglaBibliografia = new ReglaBibliografia();
        reglaCatalogos = new ReglaCatalogos(catalogClient);
    }

    private PreguntaDTO crearPreguntaValida() {
        PreguntaDTO p = new PreguntaDTO();
        p.setContexto("Contexto de prueba para examen Saber Pro");
        p.setPreguntaDirecta("¿Cuál es la capital del departamento del Cauca?");
        p.setOpciones(List.of(
                new OpcionDTO("A", "Popayán"),
                new OpcionDTO("B", "Santander de Quilichao"),
                new OpcionDTO("C", "Timbío"),
                new OpcionDTO("D", "Piendamó")
        ));
        p.setRespuestaCorrecta("A");
        p.setJustificacion("Popayán es la capital histórica y política del Cauca.");
        p.setBibliografia("Constitución Política y división político-administrativa de Colombia.");
        p.setCompetencia("Competencias Ciudadanas");
        p.setTema("Geografía Política");
        p.setSubtema("Capitales Departamentales");
        p.setNivelDificultad("Bajo");
        return p;
    }

    @Test
    @DisplayName("SSCRUM-36: Rechaza cuando faltan campos obligatorios")
    void reglaCamposObligatorios_faltanCampos() {
        PreguntaDTO p = new PreguntaDTO();
        List<ErrorValidacionDTO> errores = reglaCampos.validar(p);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> "contexto".equals(e.getCampo())));
        assertTrue(errores.stream().anyMatch(e -> "preguntaDirecta".equals(e.getCampo())));
        assertTrue(errores.stream().anyMatch(e -> "justificacion".equals(e.getCampo())));
        assertTrue(errores.stream().anyMatch(e -> "opciones".equals(e.getCampo())));
    }

    @Test
    @DisplayName("SSCRUM-37: Exige exactamente una respuesta correcta (cero respuestas)")
    void reglaRespuestaCorrecta_ceroRespuestas() {
        PreguntaDTO p = crearPreguntaValida();
        p.setRespuestaCorrecta(null);
        List<ErrorValidacionDTO> errores = reglaRespuesta.validar(p);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> "respuestaCorrecta".equals(e.getCampo())));
    }

    @Test
    @DisplayName("SSCRUM-37: Exige exactamente una respuesta correcta (múltiples respuestas no permitidas)")
    void reglaRespuestaCorrecta_multiplesRespuestas() {
        PreguntaDTO p = crearPreguntaValida();
        p.setRespuestaCorrecta("A,B");
        List<ErrorValidacionDTO> errores = reglaRespuesta.validar(p);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> "respuestaCorrecta".equals(e.getCampo())));
    }

    @Test
    @DisplayName("SSCRUM-37: Exige exactamente 4 opciones de respuesta")
    void reglaRespuestaCorrecta_cantidadOpcionesInvalida() {
        PreguntaDTO p = crearPreguntaValida();
        p.setOpciones(List.of(new OpcionDTO("A", "Op 1"), new OpcionDTO("B", "Op 2")));
        List<ErrorValidacionDTO> errores = reglaRespuesta.validar(p);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> "opciones".equals(e.getCampo())));
    }

    @Test
    @DisplayName("SSCRUM-38: Valida nivel de dificultad permitido (vacío, inválido, válido)")
    void reglaDificultad_valores() {
        PreguntaDTO p = crearPreguntaValida();

        p.setNivelDificultad(null);
        assertFalse(reglaDificultad.validar(p).isEmpty());

        p.setNivelDificultad("Extremo");
        assertFalse(reglaDificultad.validar(p).isEmpty());

        p.setNivelDificultad("Medio");
        assertTrue(reglaDificultad.validar(p).isEmpty());

        p.setNivelDificultad("Alto");
        assertTrue(reglaDificultad.validar(p).isEmpty());

        p.setNivelDificultad("Bajo");
        assertTrue(reglaDificultad.validar(p).isEmpty());
    }

    @Test
    @DisplayName("SSCRUM-39: Rechaza bibliografía vacía o de solo espacios")
    void reglaBibliografia_vaciaOSoloEspacios() {
        PreguntaDTO p = crearPreguntaValida();

        p.setBibliografia(null);
        assertFalse(reglaBibliografia.validar(p).isEmpty());

        p.setBibliografia("    ");
        assertFalse(reglaBibliografia.validar(p).isEmpty());

        p.setBibliografia("Libro de Historia de Colombia, 2022");
        assertTrue(reglaBibliografia.validar(p).isEmpty());
    }

    @Test
    @DisplayName("SSCRUM-40: Valida competencia, tema y subtema contra catálogo simulado")
    void reglaCatalogos_conCatalogoSimulado() {
        PreguntaDTO p = crearPreguntaValida();
        assertTrue(reglaCatalogos.validar(p).isEmpty());

        p.setCompetencia("Inexistente");
        catalogClient.competenciaValida = false;
        List<ErrorValidacionDTO> errores = reglaCatalogos.validar(p);
        assertFalse(errores.isEmpty());
        assertTrue(errores.stream().anyMatch(e -> "competencia".equals(e.getCampo())));
    }

    @Test
    @DisplayName("ValidationEngine registra el resultado y valida exitosamente")
    void validationEngine_validaExitosamente() {
        InMemoryResultadoRepository repo = new InMemoryResultadoRepository();
        ValidationEngine engine = new ValidationEngine(
                List.of(reglaCampos, reglaRespuesta, reglaDificultad, reglaBibliografia, reglaCatalogos),
                repo
        );

        PreguntaDTO p = crearPreguntaValida();
        ResultadoValidacionDTO resultado = engine.validarPregunta(p);

        assertTrue(resultado.isValido());
        assertTrue(resultado.getErrores().isEmpty());
        assertEquals(1, repo.saved.size());
    }
}
