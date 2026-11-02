package co.edu.unicauca.bancopreguntas.questions.service;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.contracts.dto.OpcionDTO;
import co.edu.unicauca.bancopreguntas.questions.domain.OpcionEntity;
import co.edu.unicauca.bancopreguntas.questions.domain.PreguntaBuilder;
import co.edu.unicauca.bancopreguntas.questions.domain.PreguntaEntity;
import co.edu.unicauca.bancopreguntas.questions.repository.PreguntaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class QuestionDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(QuestionDataInitializer.class);

    private final PreguntaRepository preguntaRepository;

    public QuestionDataInitializer(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }

    @Override
    public void run(String... args) {
        if (preguntaRepository.count() > 0) {
            log.info("Base de datos de preguntas ya contiene registros ({} preguntas)", preguntaRepository.count());
            return;
        }

        log.info("Iniciando migración y seeding de preguntas desde SQLite original...");
        boolean migrado = intentarMigrarDesdeSQLite();

        if (!migrado || preguntaRepository.count() == 0) {
            crearPreguntasPorDefecto();
        }

        log.info("Inicialización de preguntas completada. Total preguntas: {}", preguntaRepository.count());
    }

    private boolean intentarMigrarDesdeSQLite() {
        File[] posiblesRutas = new File[] {
                new File("saberpro.db"),
                new File("proyecto_so2_saberpro/saberpro.db"),
                new File("../proyecto_so2_saberpro/saberpro.db"),
                new File("../../proyecto_so2_saberpro/saberpro.db")
        };

        for (File dbFile : posiblesRutas) {
            if (dbFile.exists()) {
                try {
                    Class.forName("org.sqlite.JDBC");
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM preguntas")) {

                        int count = 0;
                        while (rs.next()) {
                            Long id = rs.getLong("id");
                            String contexto = rs.getString("contexto");
                            String preguntaDirecta = rs.getString("pregunta_directa");
                            String distractor1 = rs.getString("distractor1");
                            String distractor2 = rs.getString("distractor2");
                            String distractor3 = rs.getString("distractor3");
                            String distractor4 = rs.getString("distractor4");
                            String respuestaCorrecta = rs.getString("respuesta_correcta");
                            String justificacion = rs.getString("justificacion");
                            String bibliografia = rs.getString("bibliografia");
                            String competencia = rs.getString("competencia");
                            String tema = rs.getString("tema");
                            String subtema = rs.getString("subtema");
                            String nivelDificultad = rs.getString("nivel_dificultad");
                            String estadoStr = rs.getString("estado");
                            Long autorId = rs.getLong("autor_id");

                            EstadoPreguntaDTO estado = EstadoPreguntaDTO.fromString(estadoStr);

                            List<OpcionDTO> opciones = new ArrayList<>();
                            opciones.add(new OpcionDTO("A", distractor1 != null ? distractor1 : "Opción A"));
                            opciones.add(new OpcionDTO("B", distractor2 != null ? distractor2 : "Opción B"));
                            opciones.add(new OpcionDTO("C", distractor3 != null ? distractor3 : "Opción C"));
                            opciones.add(new OpcionDTO("D", distractor4 != null ? distractor4 : "Opción D"));

                            PreguntaEntity p = new PreguntaBuilder()
                                    .conId(id)
                                    .conContexto(contexto)
                                    .conPreguntaDirecta(preguntaDirecta)
                                    .conOpciones(opciones)
                                    .conRespuestaCorrecta(respuestaCorrecta)
                                    .conJustificacion(justificacion)
                                    .conBibliografia(bibliografia)
                                    .conCompetencia(competencia)
                                    .conTema(tema)
                                    .conSubtema(subtema)
                                    .conNivelDificultad(nivelDificultad)
                                    .conAutorId(autorId)
                                    .conAutorNombre("Autor " + autorId)
                                    .conEstado(estado)
                                    .conFechaCreacion(LocalDateTime.now())
                                    .build();

                            preguntaRepository.save(p);
                            count++;
                        }
                        log.info("Migradas exitosamente {} preguntas desde SQLite ({})", count, dbFile.getPath());
                        return count > 0;
                    }
                } catch (Throwable t) {
                    log.warn("No se pudo migrar directamente desde SQLite {}: {}", dbFile.getPath(), t.getMessage());
                }
            }
        }
        return false;
    }

    private void crearPreguntasPorDefecto() {
        PreguntaEntity p1 = new PreguntaBuilder()
                .conContexto("En una encuesta realizada a 200 estudiantes de ingeniería, el 60% prefiere Java.")
                .conPreguntaDirecta("¿Cuántos estudiantes prefieren Java como lenguaje principal?")
                .conOpcion("A", "120 estudiantes")
                .conOpcion("B", "100 estudiantes")
                .conOpcion("C", "140 estudiantes")
                .conOpcion("D", "80 estudiantes")
                .conRespuestaCorrecta("A")
                .conJustificacion("El 60% de 200 corresponde a (60 * 200) / 100 = 120 estudiantes.")
                .conBibliografia("Estadística Descriptiva Básica, Pearson 2021.")
                .conCompetencia("Razonamiento Cuantitativo")
                .conTema("Estadística Descriptiva")
                .conSubtema("Medidas de Tendencia Central")
                .conNivelDificultad("Bajo")
                .conAutorId(3L) // juan12
                .conAutorNombre("Juan Pérez")
                .conEstado(EstadoPreguntaDTO.BORRADOR)
                .build();

        preguntaRepository.save(p1);

        PreguntaEntity p2 = new PreguntaBuilder()
                .conContexto("La Constitución de 1991 establece los mecanismos de participación ciudadana.")
                .conPreguntaDirecta("¿Cuál es el mecanismo mediante el cual el pueblo se pronuncia sobre una ley?")
                .conOpcion("A", "Referendo")
                .conOpcion("B", "Tutela")
                .conOpcion("C", "Cabildo abierto")
                .conOpcion("D", "Iniciativa popular")
                .conRespuestaCorrecta("A")
                .conJustificacion("El referendo es la convocatoria que se hace al pueblo para que apruebe o rechace un proyecto de norma jurídica.")
                .conBibliografia("Constitución Política de Colombia, Art. 103.")
                .conCompetencia("Competencias Ciudadanas")
                .conTema("Constitución y Ciudadanía")
                .conSubtema("Mecanismos de Participación Ciudadana")
                .conNivelDificultad("Medio")
                .conAutorId(3L)
                .conAutorNombre("Juan Pérez")
                .conEstado(EstadoPreguntaDTO.PENDIENTE_REVISION)
                .build();

        preguntaRepository.save(p2);
    }
}
