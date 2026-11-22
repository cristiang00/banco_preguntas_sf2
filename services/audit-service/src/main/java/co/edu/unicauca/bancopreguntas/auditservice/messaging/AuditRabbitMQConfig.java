package co.edu.unicauca.bancopreguntas.auditservice.messaging;

import co.edu.unicauca.bancopreguntas.contracts.events.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RabbitTemplate.class)
public class AuditRabbitMQConfig {

    @Bean
    public TopicExchange saberproExchange() {
        return new TopicExchange(RabbitMQConstants.EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue auditQueue() {
        return new Queue(RabbitMQConstants.AUDIT_QUEUE, true);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange saberproExchange) {
        return BindingBuilder.bind(auditQueue).to(saberproExchange).with(RabbitMQConstants.ALL_EVENTS_ROUTING_KEY);
    }
}

