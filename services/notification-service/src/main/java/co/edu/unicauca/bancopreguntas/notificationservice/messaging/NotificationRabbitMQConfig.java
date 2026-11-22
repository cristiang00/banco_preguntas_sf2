package co.edu.unicauca.bancopreguntas.notificationservice.messaging;

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
public class NotificationRabbitMQConfig {

    @Bean
    public TopicExchange saberproExchange() {
        return new TopicExchange(RabbitMQConstants.EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue(RabbitMQConstants.NOTIFICATIONS_QUEUE, true);
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange saberproExchange) {
        return BindingBuilder.bind(notificationsQueue).to(saberproExchange).with(RabbitMQConstants.ALL_EVENTS_ROUTING_KEY);
    }
}

