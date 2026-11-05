package co.edu.unicauca.bancopreguntas.lifecycle.service;

import co.edu.unicauca.bancopreguntas.contracts.dto.EstadoPreguntaDTO;
import co.edu.unicauca.bancopreguntas.lifecycle.domain.PreguntaCicloVidaEntity;
import co.edu.unicauca.bancopreguntas.lifecycle.repository.PreguntaCicloVidaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LifecycleDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LifecycleDataInitializer.class);

    private final PreguntaCicloVidaRepository cicloVidaRepository;

    public LifecycleDataInitializer(PreguntaCicloVidaRepository cicloVidaRepository) {
        this.cicloVidaRepository = cicloVidaRepository;
    }

    @Override
    public void run(String... args) {
        if (!cicloVidaRepository.existsById(1L)) {
            cicloVidaRepository.save(new PreguntaCicloVidaEntity(1L, 3L, EstadoPreguntaDTO.BORRADOR));
            log.info("Inicializada Pregunta #1 en estado BORRADOR");
        }

        if (!cicloVidaRepository.existsById(2L)) {
            cicloVidaRepository.save(new PreguntaCicloVidaEntity(2L, 3L, EstadoPreguntaDTO.PENDIENTE_REVISION));
            log.info("Inicializada Pregunta #2 en estado PENDIENTE_REVISION");
        } else {
            // Asegurar que la pregunta #2 esté en PENDIENTE_REVISION para que pueda asignarse en pruebas
            PreguntaCicloVidaEntity p2 = cicloVidaRepository.findById(2L).orElse(null);
            if (p2 != null && p2.getEstado() != EstadoPreguntaDTO.PENDIENTE_REVISION) {
                p2.setEstado(EstadoPreguntaDTO.PENDIENTE_REVISION);
                cicloVidaRepository.save(p2);
                log.info("Sincronizada Pregunta #2 a estado PENDIENTE_REVISION");
            }
        }
    }
}

