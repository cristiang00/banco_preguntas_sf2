package co.edu.unicauca.bancopreguntas.notificationservice;

import co.edu.unicauca.bancopreguntas.contracts.events.PreguntaRechazadaEvent;
import co.edu.unicauca.bancopreguntas.notificationservice.adapter.EmailDeliveryException;
import co.edu.unicauca.bancopreguntas.notificationservice.adapter.SmtpEmailAdapter;
import co.edu.unicauca.bancopreguntas.notificationservice.factory.AprobacionNotificacionFactory;
import co.edu.unicauca.bancopreguntas.notificationservice.factory.AsignacionNotificacionFactory;
import co.edu.unicauca.bancopreguntas.notificationservice.factory.NotificacionFactoryProvider;
import co.edu.unicauca.bancopreguntas.notificationservice.factory.RechazoNotificacionFactory;
import co.edu.unicauca.bancopreguntas.notificationservice.model.NotificacionEntity;
import co.edu.unicauca.bancopreguntas.notificationservice.repository.NotificacionRepository;
import co.edu.unicauca.bancopreguntas.notificationservice.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpEmailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificacionRepository notificacionRepository;

    private SmtpEmailAdapter smtpAdapter;
    private NotificacionService notificacionService;

    @BeforeEach
    void setUp() {
        smtpAdapter = new SmtpEmailAdapter(mailSender, "localhost", 1025, "notificaciones@unicauca.edu.co");
        NotificacionFactoryProvider provider = new NotificacionFactoryProvider(List.of(
                new AsignacionNotificacionFactory(),
                new AprobacionNotificacionFactory(),
                new RechazoNotificacionFactory()
        ));
        notificacionService = new NotificacionService(notificacionRepository, provider, smtpAdapter);
    }

    @Test
    @DisplayName("Dado un servidor SMTP disponible, cuando se envía un correo, el envío es exitoso y envía SimpleMailMessage correcto")
    void testEnvioSmtpExitoso() throws EmailDeliveryException {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        boolean resultado = smtpAdapter.sendEmail("autor@unicauca.edu.co", "Prueba Asunto", "Cuerpo del correo");

        assertTrue(resultado);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertEquals("notificaciones@unicauca.edu.co", sent.getFrom());
        assertNotNull(sent.getTo());
        assertEquals("autor@unicauca.edu.co", sent.getTo()[0]);
        assertEquals("Prueba Asunto", sent.getSubject());
        assertEquals("Cuerpo del correo", sent.getText());
    }

    @Test
    @DisplayName("Dado un servidor SMTP caído, cuando se envía un correo, se lanza EmailDeliveryException")
    void testEnvioSmtpServidorCaido() {
        doThrow(new MailSendException("Connection refused: connect")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(EmailDeliveryException.class, () ->
                smtpAdapter.sendEmail("destinatario@unicauca.edu.co", "Fallo", "Mensaje"));
    }

    @Test
    @DisplayName("Dado que el servidor SMTP falla, la notificación se registra como FALLIDA y NO rompe el flujo general")
    void testFalloSmtpNoRompeFlujo() {
        doThrow(new MailSendException("Servidor SMTP inalcanzable")).when(mailSender).send(any(SimpleMailMessage.class));
        when(notificacionRepository.existsByEventoIdAndDestinatario(anyString(), anyString())).thenReturn(false);
        when(notificacionRepository.save(any(NotificacionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PreguntaRechazadaEvent event = new PreguntaRechazadaEvent(
                99L, 10L, "Autor Juan", "autor.juan@unicauca.edu.co",
                "Contexto de prueba", List.of("Observación de rechazo"), "corr-test-123"
        );

        assertDoesNotThrow(() -> notificacionService.procesarEvento(event));

        ArgumentCaptor<NotificacionEntity> captor = ArgumentCaptor.forClass(NotificacionEntity.class);
        verify(notificacionRepository, atLeastOnce()).save(captor.capture());
        NotificacionEntity guardada = captor.getValue();
        assertEquals("FALLIDA", guardada.getEstado());
        assertEquals("autor.juan@unicauca.edu.co", guardada.getDestinatario());
    }
}
