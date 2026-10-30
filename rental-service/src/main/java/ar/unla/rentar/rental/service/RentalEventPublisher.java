package ar.unla.rentar.rental.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import ar.unla.rentar.rental.dto.ReservaConfirmadaEvent;

@Component
public class RentalEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE_NAME = "rentar.events";

    public RentalEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarReservaConfirmada(ReservaConfirmadaEvent evento) {
        rabbitTemplate.convertAndSend("rentar.maintenance.request", evento);
    }
}
