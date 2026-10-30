package ar.unla.rentar.vehicle.service;
// Publicador de eventos relacionados con vehículos, como la solicitud de verificación.
import ar.unla.rentar.vehicle.events.EventEnvelope;
import ar.unla.rentar.vehicle.events.VehiculoVerificacionSolicitada;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import ar.unla.rentar.vehicle.events.VehiculoExternoRegistrado;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class VehicleEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE_NAME = "rentar.events";

    @Autowired
    public VehicleEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    //verificación de vehículos
    public void publicarSolicitudVerificacion(Long vehiculoId, String patente) {
        VehiculoVerificacionSolicitada payload = new VehiculoVerificacionSolicitada(vehiculoId, patente);
        
        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                "vehiculos.verificacion.solicitada",
                UUID.randomUUID().toString(),
                Instant.now().toString(),
                payload
        );

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "vehiculos.verificacion.solicitada", envelope);
    }

    //vehículos externos 
    public void publishVehiculoExternoRegistrado(VehiculoExternoRegistrado evento) {
        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                "vehiculos.externos.registrados",
                UUID.randomUUID().toString(),
                Instant.now().toString(),
                evento
        );

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "vehiculos.externos.registrados", envelope);
        System.out.println("Evento externo enviado a RabbitMQ - Patente: " + evento.getPatente());
    }
}