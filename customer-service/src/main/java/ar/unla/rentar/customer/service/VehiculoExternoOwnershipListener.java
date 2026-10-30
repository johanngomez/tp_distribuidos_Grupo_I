package ar.unla.rentar.customer.service;

import ar.unla.rentar.customer.events.EventEnvelope;
import ar.unla.rentar.customer.events.VehiculoExternoValidacionTitularidad;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class VehiculoExternoOwnershipListener {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RegistroTitularidadService registroTitularidadService; 

    public VehiculoExternoOwnershipListener(RabbitTemplate rabbitTemplate, 
                                            ObjectMapper objectMapper, 
                                            RegistroTitularidadService registroTitularidadService) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.registroTitularidadService = registroTitularidadService;
    }

    @RabbitListener(queues = "rentar.external.vehicle.ownership.request")
    public void procesarValidacionTitularidad(EventEnvelope eventoRecibido) {
        try {
            System.out.println("-> Recibido evento para validar titularidad. CorrelationId: " + eventoRecibido.getCorrelationId());

            Map<String, Object> payloadMap = (Map<String, Object>) eventoRecibido.getPayload();
            Long vehiculoExternoId = ((Number) payloadMap.get("vehiculoExternoId")).longValue();
            Long clienteId = ((Number) payloadMap.get("clienteId")).longValue();

            String resultado = registroTitularidadService.validarTitularidad(clienteId);
            System.out.println("-> Resultado de titularidad para cliente " + clienteId + ": " + resultado);

            VehiculoExternoValidacionTitularidad resultadoPayload = new VehiculoExternoValidacionTitularidad(
                    vehiculoExternoId,
                    clienteId,
                    resultado
            );

            EventEnvelope eventoSalida = new EventEnvelope();
            eventoSalida.setEventId(UUID.randomUUID().toString());
            eventoSalida.setEventType("vehiculos.externos.validacion.titularidad");
            eventoSalida.setCorrelationId(eventoRecibido.getCorrelationId());
            eventoSalida.setOccurredAt(Instant.now().toString());
            eventoSalida.setPayload(resultadoPayload);

            rabbitTemplate.convertAndSend("rentar.events", "vehiculos.externos.validacion.titularidad", eventoSalida);
            
            System.out.println("-> Evento de validación de titularidad publicado con éxito.");

        } catch (Exception e) {
            System.err.println("-> Error procesando validación de titularidad: " + e.getMessage());
        }
    }
}
