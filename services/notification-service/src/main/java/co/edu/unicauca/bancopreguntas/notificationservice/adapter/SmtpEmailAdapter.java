package co.edu.unicauca.bancopreguntas.notificationservice.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Adaptador para envío real de correos mediante protocolo SMTP (GoF Adapter).
 * Compatible con servidores SMTP de producción o servidores de prueba (Mailpit / MailHog).
 */
@Component
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "smtp", matchIfMissing = false)
public class SmtpEmailAdapter implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailAdapter.class);

    @Value("${notification.smtp.host:${SMTP_HOST:localhost}}")
    private String host;

    @Value("${notification.smtp.port:${SMTP_PORT:1025}}")
    private int port;

    @Value("${notification.smtp.username:${SMTP_USER:}}")
    private String username;

    @Value("${notification.smtp.password:${SMTP_PASS:}}")
    private String password;

    @Value("${notification.smtp.from:${SMTP_FROM:notificaciones@unicauca.edu.co}}")
    private String from;

    private JavaMailSender mailSender;

    public SmtpEmailAdapter() {}

    public SmtpEmailAdapter(JavaMailSender mailSender, String host, int port, String from) {
        this.mailSender = mailSender;
        this.host = host;
        this.port = port;
        this.from = from;
    }

    public synchronized JavaMailSender getMailSender() {
        if (this.mailSender == null) {
            JavaMailSenderImpl impl = new JavaMailSenderImpl();
            impl.setHost(this.host != null ? this.host : "localhost");
            impl.setPort(this.port > 0 ? this.port : 1025);
            if (this.username != null && !this.username.isBlank()) {
                impl.setUsername(this.username);
                impl.setPassword(this.password != null ? this.password : "");
                Properties props = impl.getJavaMailProperties();
                props.put("mail.smtp.auth", "true");
            }
            Properties props = impl.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.connectiontimeout", "3000");
            props.put("mail.smtp.timeout", "3000");
            this.mailSender = impl;
        }
        return this.mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean sendEmail(String to, String subject, String body) throws EmailDeliveryException {
        try {
            log.info("[SmtpEmailAdapter] Enviando correo SMTP a {}:{} desde='{}' para destinatario='{}'",
                    host, port, from, to);
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            getMailSender().send(msg);
            log.info("[SmtpEmailAdapter] Correo SMTP enviado exitosamente a '{}'", to);
            return true;
        } catch (Exception e) {
            log.warn("[SmtpEmailAdapter] Falló el envío de correo SMTP a '{}': {}", to, e.getMessage());
            throw new EmailDeliveryException("Fallo en entrega de correo SMTP hacia " + to + " (" + host + ":" + port + ")", e);
        }
    }

    @Override
    public String getProviderName() {
        return "SMTP (" + host + ":" + port + ")";
    }
}
