package co.edu.unicauca.bancopreguntas.auditservice;

import co.edu.unicauca.bancopreguntas.auditservice.messaging.AuditRabbitListener;
import co.edu.unicauca.bancopreguntas.auditservice.service.AuditService;
import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditRabbitListenerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditRabbitListener auditRabbitListener;

    @Test
    @DisplayName("Dado que llega un mensaje JSON a la cola de auditoría de RabbitMQ, el listener registra el evento en la bitácora")
    void testRecibirMensajeRabbitMQAuditoria() throws Exception {
        PreguntaRechazadaEvent event = new PreguntaRechazadaEvent(
                202L, 15L, "Autor Ana", "ana@unicauca.edu.co",
                "Contexto de pregunta rechazada", List.of("Falta bibliografía"), "corr-audit-amqp"
        );
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        String json = mapper.writeValueAsString(event);

        auditRabbitListener.receiveMessage(json);

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(auditService, times(1)).registrarEvento(captor.capture());

        DomainEvent auditado = captor.getValue();
        assertEquals("PreguntaRechazada", auditado.getEventType());
        assertEquals("202", auditado.getAggregateId());
        assertEquals("corr-audit-amqp", auditado.getCorrelationId());
    }
}
