package ar.unla.rentar.vehicle.service;

import ar.unla.rentar.vehicle.events.VehiculoExternoRegistrado;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "rentar.events";
    
    private static final String ROUTING_KEY = "vehiculos.externos.registrados";

    public void publishVehiculoExternoRegistrado(VehiculoExternoRegistrado evento) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, evento);
        System.out.println("Evento enviado a RabbitMQ - Patente: " + evento.getPatente());
    }
}