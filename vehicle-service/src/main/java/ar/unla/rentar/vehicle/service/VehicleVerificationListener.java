package ar.unla.rentar.vehicle.service;
// consumidor de solicitudes de verificación de vehículos y publicación de resultados
import ar.unla.rentar.vehicle.events.EventEnvelope;
import ar.unla.rentar.vehicle.events.VehiculoVerificacionResultado;
import ar.unla.rentar.vehicle.events.VehiculoVerificacionSolicitada;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class VehicleVerificationListener {

    private static final Logger logger = LoggerFactory.getLogger(VehicleVerificationListener.class);

    private final RegistroPolicialService registroPolicialService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final String EXCHANGE_NAME = "rentar.events";

    public VehicleVerificationListener(RegistroPolicialService registroPolicialService,
                                       RabbitTemplate rabbitTemplate,
                                       ObjectMapper objectMapper) {
        this.registroPolicialService = registroPolicialService;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }
    
   @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "rentar.vehicle.verification", durable = "true"),
        exchange = @Exchange(value = "rentar.events", type = "topic"),
        key = "vehiculos.verificacion.solicitada"
    ))
    public void procesarSolicitudVerificacion(EventEnvelope envelope) {
        try {
            // Validación defensiva del mensaje entrante
            if (envelope == null || envelope.getPayload() == null) {
                logger.error("El envelope recibido o su payload son nulos. Descartando mensaje.");
                // AmqpRejectAndDontRequeueException descarta el mensaje sin reencolarlo (evita bucle infinito)
                throw new AmqpRejectAndDontRequeueException("Payload nulo");
            }

            // Mapeamos el payload genérico al objeto específico
            VehiculoVerificacionSolicitada solicitud = objectMapper.convertValue(
                    envelope.getPayload(), 
                    VehiculoVerificacionSolicitada.class
            );

            if (solicitud.getVehiculoId() == null || solicitud.getPatente() == null) {
                logger.error("Datos incompletos en la solicitud de verificación: {}", solicitud);
                throw new AmqpRejectAndDontRequeueException("Datos de vehículo incompletos");
            }

            logger.info("Procesando verificación para vehículo ID: {} con patente: {}", 
                    solicitud.getVehiculoId(), solicitud.getPatente());

            // 1. Consultamos el registro policial simulado
            String resultadoVerificacion = registroPolicialService.verificarVehiculo(solicitud.getPatente());

            // 2. Preparamos el payload de respuesta
            VehiculoVerificacionResultado resultadoPayload = new VehiculoVerificacionResultado(
                    solicitud.getVehiculoId(),
                    solicitud.getPatente(),
                    resultadoVerificacion
            );

            // 3. Armamos el envelope para el evento de resultado
            EventEnvelope resultadoEnvelope = new EventEnvelope(
                    UUID.randomUUID().toString(),
                    "vehiculos.verificacion.resultado",
                    envelope.getCorrelationId() != null ? envelope.getCorrelationId() : UUID.randomUUID().toString(),
                    Instant.now().toString(),
                    resultadoPayload
            );

            // 4. Publicamos el resultado al exchange
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, "vehiculos.verificacion.resultado", resultadoEnvelope);
            logger.info("Resultado '{}' publicado con éxito para el vehículo ID: {}", 
                    resultadoVerificacion, solicitud.getVehiculoId());

        } catch (AmqpRejectAndDontRequeueException e) {
            // Errores graves o de formato que no deben reintentarse
            logger.error("Error definitivo procesando el mensaje de verificación: {}", e.getMessage());
            throw e; 
        } catch (Exception e) {
            // Errores inesperados (ej: problemas de conexión a BD si los hubiera, etc.)
            logger.error("Error temporal/inesperado al procesar la verificación del vehículo.", e);
            // Al dejar propagar la excepción (sin envolverla en AmqpReject...), 
            // Spring AMQP manejará el reintento según la configuración del broker.
            throw new RuntimeException("Error en procesamiento asíncrono", e);
        }
    }
}


