package ar.unla.rentar.vehicle.service;

import ar.unla.rentar.vehicle.events.EventEnvelope;
import ar.unla.rentar.vehicle.events.VehiculoExternoValidacionPatente;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class VehiculoExternoPatentListener {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RegistroPolicialService registroPolicialService;

    //se inyectan las herramientas que se necesitan para mandar mensajes y leer JSON
    public VehiculoExternoPatentListener(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper,
                                         RegistroPolicialService registroPolicialService) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.registroPolicialService = registroPolicialService;
    }

    @RabbitListener(queues = "rentar.external.vehicle.patent.request")    
    public void procesarValidacionPatente(EventEnvelope eventoRecibido) {
        try {
            System.out.println("-> [PatentListener] Recibido evento para validar patente. CorrelationId: " + eventoRecibido.getCorrelationId());

            Map<String, Object> payloadMap = (Map<String, Object>) eventoRecibido.getPayload();
            Long vehiculoExternoId = ((Number) payloadMap.get("vehiculoExternoId")).longValue();
            String patente = (String) payloadMap.get("patente");

            //lógica de validación simulada: si la patente no está vacía, la damos por válida.
            String verificacion = registroPolicialService.verificarVehiculo(patente);
            String resultado = "ROBADO".equals(verificacion) ? "RECHAZADO" : "VALIDADO";
            System.out.println("-> [PatentListener] Resultado de patente " + patente + ": " + resultado);

            VehiculoExternoValidacionPatente resultadoPayload = new VehiculoExternoValidacionPatente(
                    vehiculoExternoId,
                    patente,
                    resultado
            );

            EventEnvelope eventoSalida = new EventEnvelope();
            eventoSalida.setEventId(UUID.randomUUID().toString());
            eventoSalida.setEventType("vehiculos.externos.validacion.patente");
            eventoSalida.setCorrelationId(eventoRecibido.getCorrelationId()); // CLAVE: Mismo ID de proceso
            eventoSalida.setOccurredAt(Instant.now().toString());
            eventoSalida.setPayload(resultadoPayload);

            rabbitTemplate.convertAndSend("rentar.events", "vehiculos.externos.validacion.patente", eventoSalida);
            
            System.out.println("-> [PatentListener] Evento de resultado de patente publicado con éxito.");

        } catch (Exception e) {
            System.err.println("-> [PatentListener] Error procesando validación de patente: " + e.getMessage());
        }
    }
}
