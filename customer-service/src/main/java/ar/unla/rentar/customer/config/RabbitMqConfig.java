package ar.unla.rentar.customer.config;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.beans.factory.annotation.Qualifier;

/** Declaraciones compartidas del transporte de eventos del Hito 3. */
@Configuration
public class RabbitMqConfig {

    public static final String EVENTS_EXCHANGE = "rentar.events";
    public static final String EXTERNAL_VEHICLE_OWNERSHIP_QUEUE = "rentar.external.vehicle.ownership.request";

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
    public Queue externalVehicleOwnershipQueue() {
        return new Queue(EXTERNAL_VEHICLE_OWNERSHIP_QUEUE, true);
    }

    @Bean
    public Binding externalVehicleOwnershipBinding(
            @Qualifier("externalVehicleOwnershipQueue") Queue externalVehicleOwnershipQueue,
            @Qualifier("eventsExchange") TopicExchange eventsExchange) {
        return BindingBuilder.bind(externalVehicleOwnershipQueue)
                .to(eventsExchange)
                .with("vehiculos.externos.registrados");
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
