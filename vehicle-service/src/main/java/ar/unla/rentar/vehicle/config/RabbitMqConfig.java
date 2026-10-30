package ar.unla.rentar.vehicle.config;

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

@Configuration
public class RabbitMqConfig {

    public static final String EVENTS_EXCHANGE = "rentar.events";

    public static final String VEHICLE_VERIFICATION_QUEUE = "rentar.vehicle.verification";
    public static final String VEHICLE_VERIFICATION_RESULT_QUEUE = "rentar.vehicle.verification.result";
    public static final String EXTERNAL_VEHICLE_PATENT_QUEUE = "rentar.external.vehicle.patent.request";
    public static final String EXTERNAL_VEHICLE_PATENT_RESULT_QUEUE = "rentar.external.vehicle.patent";
    public static final String EXTERNAL_VEHICLE_OWNERSHIP_RESULT_QUEUE = "rentar.external.vehicle.ownership";
    public static final String EXTERNAL_VEHICLE_COMPLETED_QUEUE = "rentar.external.vehicle.completed";

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
    public Queue vehicleVerificationQueue() {
        return new Queue(VEHICLE_VERIFICATION_QUEUE, true);
    }

    @Bean
    public Binding vehicleVerificationBinding(@Qualifier("vehicleVerificationQueue") Queue vehicleVerificationQueue,
                                              @Qualifier("eventsExchange") TopicExchange eventsExchange) {
        return BindingBuilder.bind(vehicleVerificationQueue)
                .to(eventsExchange)
                .with("vehiculos.verificacion.solicitada");
    }

    @Bean
    public Queue vehicleVerificationResultQueue() {
        return new Queue(VEHICLE_VERIFICATION_RESULT_QUEUE, true);
    }

    @Bean
    public Binding vehicleVerificationResultBinding(
            @Qualifier("vehicleVerificationResultQueue") Queue vehicleVerificationResultQueue,
            @Qualifier("eventsExchange") TopicExchange eventsExchange) {
        return BindingBuilder.bind(vehicleVerificationResultQueue)
                .to(eventsExchange)
                .with("vehiculos.verificacion.resultado");
    }

    @Bean
    public Queue externalVehiclePatentQueue() {
        return new Queue(EXTERNAL_VEHICLE_PATENT_QUEUE, true);
    }

    @Bean
    public Binding externalVehiclePatentBinding(@Qualifier("externalVehiclePatentQueue") Queue externalVehiclePatentQueue,
                                                @Qualifier("eventsExchange") TopicExchange eventsExchange) {
        return BindingBuilder.bind(externalVehiclePatentQueue)
                .to(eventsExchange)
                .with("vehiculos.externos.registrados");
    }

    @Bean
    public Queue externalVehiclePatentResultQueue() {
        return new Queue(EXTERNAL_VEHICLE_PATENT_RESULT_QUEUE, true);
    }

    @Bean
    public Binding externalVehiclePatentResultBinding(
            @Qualifier("externalVehiclePatentResultQueue") Queue queue,
            @Qualifier("eventsExchange") TopicExchange eventsExchange) {
        return BindingBuilder.bind(queue)
                .to(eventsExchange)
                .with("vehiculos.externos.validacion.patente");
    }

    @Bean
    public Queue externalVehicleOwnershipResultQueue() {
        return new Queue(EXTERNAL_VEHICLE_OWNERSHIP_RESULT_QUEUE, true);
    }

    @Bean
    public Binding externalVehicleOwnershipResultBinding(
            @Qualifier("externalVehicleOwnershipResultQueue") Queue queue,
            @Qualifier("eventsExchange") TopicExchange eventsExchange) {
        return BindingBuilder.bind(queue)
                .to(eventsExchange)
                .with("vehiculos.externos.validacion.titularidad");
    }

    @Bean
    public Queue externalVehicleCompletedQueue() {
        return new Queue(EXTERNAL_VEHICLE_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding externalVehicleCompletedBinding(
            @Qualifier("externalVehicleCompletedQueue") Queue externalVehicleCompletedQueue,
            @Qualifier("eventsExchange") TopicExchange eventsExchange) {
        return BindingBuilder.bind(externalVehicleCompletedQueue)
                .to(eventsExchange)
                .with("vehiculos.externos.validacion.completada");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
