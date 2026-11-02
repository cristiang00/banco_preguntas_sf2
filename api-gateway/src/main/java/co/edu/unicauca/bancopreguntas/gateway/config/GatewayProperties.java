package co.edu.unicauca.bancopreguntas.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gateway.services")
public class GatewayProperties {

    private String users = "http://localhost:8081";
    private String questions = "http://localhost:8082";
    private String validation = "http://localhost:8083";
    private String lifecycle = "http://localhost:8084";
    private String review = "http://localhost:8085";
    private String notification = "http://localhost:8086";
    private String audit = "http://localhost:8087";

    public String getUsers() { return users; }
    public void setUsers(String users) { this.users = users; }

    public String getQuestions() { return questions; }
    public void setQuestions(String questions) { this.questions = questions; }

    public String getValidation() { return validation; }
    public void setValidation(String validation) { this.validation = validation; }

    public String getLifecycle() { return lifecycle; }
    public void setLifecycle(String lifecycle) { this.lifecycle = lifecycle; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public String getNotification() { return notification; }
    public void setNotification(String notification) { this.notification = notification; }

    public String getAudit() { return audit; }
    public void setAudit(String audit) { this.audit = audit; }
}
