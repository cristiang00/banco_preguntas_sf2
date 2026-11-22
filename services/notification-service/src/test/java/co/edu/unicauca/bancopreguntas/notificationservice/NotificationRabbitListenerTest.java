package co.edu.unicauca.bancopreguntas.notificationservice;

import co.edu.unicauca.bancopreguntas.contracts.events.DomainEvent;
import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaAprobadaEvent;
import co.edu.unicauca.bancopreguntas.notificationservice.messaging.NotificationRabbitListener;
import co.edu.unicauca.bancopreguntas.notificationservice.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRabbitListenerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificationRabbitListener rabbitListener;

    @Test
    @DisplayName("Dado que llega un mensaje JSON a la cola de RabbitMQ, el listener lo deserializa y procesa")
    void testRecibirMensajeRabbitMQ() throws Exception {
        PreguntaAprobadaEvent event = new PreguntaAprobadaEvent(
                101L, 20L, "Autor Carlos", "carlos@unicauca.edu.co",
                "Contexto de pregunta aprobada", "Consenso unánime", "corr-amqp-1"
        );
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        String json = mapper.writeValueAsString(event);

        rabbitListener.receiveMessage(json);

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(notificacionService, times(1)).procesarEvento(captor.capture());

        DomainEvent procesado = captor.getValue();
        assertEquals("PreguntaAprobada", procesado.getEventType());
        assertEquals("101", procesado.getAggregateId());
        assertEquals("corr-amqp-1", procesado.getCorrelationId());
    }
}
