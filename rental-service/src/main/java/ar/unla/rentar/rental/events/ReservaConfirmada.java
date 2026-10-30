package ar.unla.rentar.rental.events;
import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import ar.unla.rentar.rental.dto.ReservaConfirmadaEvent;

@Service
public class ReservaConfirmada {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE_NAME = "rentar.events";

    public void publicarReservaConfirmada(Long reservaId, Long vehiculoId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {

        ReservaConfirmadaEvent evento = new ReservaConfirmadaEvent(reservaId, vehiculoId, fechaInicio, fechaFin);

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "reserva.confirmada", evento);


    }
}
