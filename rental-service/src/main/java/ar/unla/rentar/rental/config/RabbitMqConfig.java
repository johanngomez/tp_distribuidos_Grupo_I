package ar.unla.rentar.rental.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

/** Declaraciones compartidas del transporte de eventos del Hito 3. */
@Configuration
public class RabbitMqConfig {

    public static final String EVENTS_EXCHANGE = "rentar.events";
    public static final String MAINTENANCE_REQUEST_QUEUE = "rentar.maintenance.request";
    public static final String MAINTENANCE_RESULT_QUEUE = "rentar.maintenance.result";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ApplicationRunner declareEventsExchange(AmqpAdmin amqpAdmin, TopicExchange eventsExchange) {
        return args -> amqpAdmin.declareExchange(eventsExchange);
    }

    @Bean
    public Queue maintenanceRequestQueue() {
        return new Queue(MAINTENANCE_REQUEST_QUEUE, true);
    }

    @Bean
    public Binding maintenanceRequestBinding(@Qualifier("maintenanceRequestQueue") Queue maintenanceRequestQueue,
                                             @Qualifier("eventsExchange") TopicExchange eventsExchange) {
        return BindingBuilder.bind(maintenanceRequestQueue)
                .to(eventsExchange)
                .with("reserva.confirmada");
    }

    @Bean
    public Queue maintenanceResultQueue() {
        return new Queue(MAINTENANCE_RESULT_QUEUE, true);
    }

    @Bean
    public Binding maintenanceResultBinding(@Qualifier("maintenanceResultQueue") Queue maintenanceResultQueue,
                                            @Qualifier("eventsExchange") TopicExchange eventsExchange) {
        return BindingBuilder.bind(maintenanceResultQueue)
                .to(eventsExchange)
                .with("mantenimiento.resultado");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
