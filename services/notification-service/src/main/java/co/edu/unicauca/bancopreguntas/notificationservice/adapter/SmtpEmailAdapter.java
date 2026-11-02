package co.edu.unicauca.bancopreguntas.notificationservice.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adaptador para cliente SMTP (GoF Adapter).
 */
@Component
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "smtp")
public class SmtpEmailAdapter implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailAdapter.class);

    @Value("${notification.smtp.host:localhost}")
    private String host;

    @Value("${notification.smtp.port:25}")
    private int port;

    @Value("${notification.smtp.username:}")
    private String username;

    @Override
    public boolean sendEmail(String to, String subject, String body) throws EmailDeliveryException {
        try {
            log.info("[SmtpEmailAdapter] Enviando correo SMTP a {}:{} hacia destinatario={}", host, port, to);
            // Protocolo SMTP estándar simulado o conexión real
            return true;
        } catch (Exception e) {
            throw new EmailDeliveryException("Error conectando al servidor SMTP: " + host + ":" + port, e);
        }
    }

    @Override
    public String getProviderName() {
        return "SMTP (" + host + ":" + port + ")";
    }
}

