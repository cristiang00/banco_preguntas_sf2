package co.edu.unicauca.bancopreguntas.notificationservice.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adaptador para cliente de correo simulado/registro (GoF Adapter).
 * Permite ejecutar pruebas y desarrollo local sin requerir un servidor SMTP externo.
 */
@Component
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "mock", matchIfMissing = true)
public class MockEmailAdapter implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(MockEmailAdapter.class);

    private final List<SentEmailRecord> sentEmails = Collections.synchronizedList(new ArrayList<>());
    private boolean simulateFailure = false;

    public static class SentEmailRecord {
        private final String to;
        private final String subject;
        private final String body;

        public SentEmailRecord(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        public String getTo() { return to; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
    }

    @Override
    public boolean sendEmail(String to, String subject, String body) throws EmailDeliveryException {
        if (simulateFailure) {
            log.error("[MockEmailAdapter] Fallo simulado al enviar email a {}", to);
            throw new EmailDeliveryException("Fallo inducido en el servicio de transporte de correo");
        }
        sentEmails.add(new SentEmailRecord(to, subject, body));
        log.info("[MockEmailAdapter] >>> Email simulado enviado a '{}' | Asunto: '{}' | Longitud cuerpo: {} caracteres",
                to, subject, body != null ? body.length() : 0);
        return true;
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    public List<SentEmailRecord> getSentEmails() {
        return Collections.unmodifiableList(sentEmails);
    }

    public void clear() {
        sentEmails.clear();
        simulateFailure = false;
    }

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }
}
