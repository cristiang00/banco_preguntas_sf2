package co.edu.unicauca.bancopreguntas.review.infrastructure.rest;

import co.edu.unicauca.bancopreguntas.contracts.dto.PreguntaDTO;
import co.edu.unicauca.bancopreguntas.review.application.ports.output.PreguntasClientPort;
import co.edu.unicauca.bancopreguntas.review.client.QuestionsServiceClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class QuestionsClientAdapter implements PreguntasClientPort {

    private final QuestionsServiceClient client;

    public QuestionsClientAdapter(QuestionsServiceClient client) {
        this.client = client;
    }

    @Override
    public Optional<PreguntaDTO> obtenerPregunta(Long preguntaId) {
        return client.obtenerPregunta(preguntaId);
    }

    @Override
    public void actualizarProyeccionEstado(Long preguntaId, String nuevoEstado) {
        client.actualizarProyeccionEstado(preguntaId, nuevoEstado);
    }

    @Override
    public List<PreguntaDTO> listarPreguntasPendientes() {
        return client.listarPreguntasPendientes();
    }
}

