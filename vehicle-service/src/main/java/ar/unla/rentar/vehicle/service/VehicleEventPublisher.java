package ar.unla.rentar.vehicle.service;
// Publicador de eventos relacionados con vehículos, como la solicitud de verificación.
import ar.unla.rentar.vehicle.events.EventEnvelope;
import ar.unla.rentar.vehicle.events.VehiculoVerificacionSolicitada;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class VehicleEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    // El exchange común definido por el equipo 1
    private static final String EXCHANGE_NAME = "rentar.events";

    public VehicleEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

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
}

