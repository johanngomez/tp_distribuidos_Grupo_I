package ar.unla.rentar.vehicle.service;

import ar.unla.rentar.vehicle.events.EventEnvelope;
import ar.unla.rentar.vehicle.events.VehiculoExternoValidacionCompletada;
import ar.unla.rentar.vehicle.events.VehiculoExternoValidacionPatente;
import ar.unla.rentar.vehicle.events.VehiculoExternoValidacionTitularidad;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Correlaciona las validaciones independientes de un vehículo externo. */
@Component
public class VehiculoExternoValidationAggregator {

    private static final String EXCHANGE_NAME = "rentar.events";
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, ValidationState> pending = new ConcurrentHashMap<>();

    public VehiculoExternoValidationAggregator(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "rentar.external.vehicle.patent")
    public void receivePatentResult(EventEnvelope envelope) {
        VehiculoExternoValidacionPatente result = objectMapper.convertValue(
                envelope.getPayload(), VehiculoExternoValidacionPatente.class);
        String key = correlationKey(envelope, result.getVehiculoExternoId());
        pending.computeIfAbsent(key, ignored -> new ValidationState(result.getVehiculoExternoId()))
                .setPatent(result.getResultado());
        completeIfReady(key);
    }

    @RabbitListener(queues = "rentar.external.vehicle.ownership")
    public void receiveOwnershipResult(EventEnvelope envelope) {
        VehiculoExternoValidacionTitularidad result = objectMapper.convertValue(
                envelope.getPayload(), VehiculoExternoValidacionTitularidad.class);
        String key = correlationKey(envelope, result.getVehiculoExternoId());
        pending.computeIfAbsent(key, ignored -> new ValidationState(result.getVehiculoExternoId()))
                .setOwnership(result.getResultado());
        completeIfReady(key);
    }

    private void completeIfReady(String key) {
        ValidationState state = pending.get(key);
        if (state == null || !state.markCompleted()) {
            return;
        }

        String finalResult = "VALIDADO".equalsIgnoreCase(state.patent)
                && "VALIDADO".equalsIgnoreCase(state.ownership)
                ? "VALIDADO" : "RECHAZADO";

        EventEnvelope completed = new EventEnvelope(
                UUID.randomUUID().toString(),
                "vehiculos.externos.validacion.completada",
                key,
                Instant.now().toString(),
                new VehiculoExternoValidacionCompletada(state.vehicleId, finalResult));

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "vehiculos.externos.validacion.completada", completed);
        pending.remove(key, state);
    }

    private String correlationKey(EventEnvelope envelope, Long vehicleId) {
        return envelope.getCorrelationId() == null || envelope.getCorrelationId().isBlank()
                ? "vehiculo:" + vehicleId : envelope.getCorrelationId();
    }

    private static final class ValidationState {
        private final Long vehicleId;
        private String patent;
        private String ownership;
        private boolean completed;

        private ValidationState(Long vehicleId) {
            this.vehicleId = vehicleId;
        }

        private synchronized ValidationState setPatent(String value) {
            this.patent = value;
            return this;
        }

        private synchronized ValidationState setOwnership(String value) {
            this.ownership = value;
            return this;
        }

        private synchronized boolean markCompleted() {
            if (completed || patent == null || ownership == null) {
                return false;
            }
            completed = true;
            return true;
        }
    }
}
