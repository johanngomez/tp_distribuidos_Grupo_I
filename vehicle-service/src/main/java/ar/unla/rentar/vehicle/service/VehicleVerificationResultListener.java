package ar.unla.rentar.vehicle.service;
// consumidor de resultados de verificación de vehículos y actualización del estado en la base de datos
import ar.unla.rentar.vehicle.events.EventEnvelope;
import ar.unla.rentar.vehicle.events.VehiculoVerificacionResultado;
import ar.unla.rentar.vehicle.model.Vehiculo; 
import ar.unla.rentar.vehicle.repository.VehiculoRepository; 
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Component
public class VehicleVerificationResultListener {

    private static final Logger logger = LoggerFactory.getLogger(VehicleVerificationResultListener.class);

    private final VehiculoRepository vehiculoRepository;
    private final ObjectMapper objectMapper;

    public VehicleVerificationResultListener(VehiculoRepository vehiculoRepository, ObjectMapper objectMapper) {
        this.vehiculoRepository = vehiculoRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "rentar.vehicle.verification.result")
    public void procesarResultadoVerificacion(EventEnvelope envelope) {
        try {
            // 1. Validación defensiva del mensaje
            if (envelope == null || envelope.getPayload() == null) {
                logger.error("El envelope del resultado o su payload son nulos. Descartando mensaje.");
                throw new AmqpRejectAndDontRequeueException("Payload de resultado nulo");
            }

            // 2. Mapeamos al objeto de resultado
            VehiculoVerificacionResultado resultado = objectMapper.convertValue(
                    envelope.getPayload(),
                    VehiculoVerificacionResultado.class
            );

            if (resultado.getVehiculoId() == null || resultado.getResultado() == null) {
                logger.error("Datos incompletos en el resultado de verificación: {}", resultado);
                throw new AmqpRejectAndDontRequeueException("Datos de resultado incompletos");
            }
            // Logueamos el resultado recibido para trazabilidad
            logger.info("Recibiendo resultado de verificación '{}' para vehículo ID: {}", 
                    resultado.getResultado(), resultado.getVehiculoId());

            // 3. Buscamos el vehículo en la base de datos de vehículos (rentar_vehiculos)
            Optional<Vehiculo> optionalVehiculo = vehiculoRepository.findById(resultado.getVehiculoId());

            if (optionalVehiculo.isEmpty()) {
                logger.error("Vehículo con ID {} no encontrado en la base de datos local.", resultado.getVehiculoId());
                // Si el vehículo no existe, no se reintenta.
                throw new AmqpRejectAndDontRequeueException("Vehículo no encontrado en la base de datos " + resultado.getVehiculoId());
            }

            Vehiculo vehiculo = optionalVehiculo.get(); // obtenemos el vehículo para actualizar segun resultado de verificación

            // 4. Aplicamos la regla de negocio según el resultado
            if ("ROBADO".equalsIgnoreCase(resultado.getResultado().trim())) {
                // Si es robado, lo inhabilitamos / damos de baja lógica para nuevas operaciones
                vehiculo.setActivo(false); 
                
                logger.warn("¡ATENCIÓN! El vehículo ID {} ({}) fue marcado como ROBADO y dado de baja lógica (activo: 0).", 
                        vehiculo.getId(), vehiculo.getPatente());
            } else {
                // Si es VALIDADO, nos aseguramos que esté activo normalmente
                vehiculo.setActivo(true);
                logger.info("Vehículo ID {} validado correctamente y activo.", vehiculo.getId());
            }

            // 5. Guardamos los cambios en la base de datos
            vehiculoRepository.save(vehiculo);
            logger.info("Estado del vehículo ID {} actualizado con éxito en la base de datos.", vehiculo.getId());

            //si hay algún error crítico, lo registramos y lanzamos la excepción para que el mensaje no se reencole
        } catch (AmqpRejectAndDontRequeueException e) {
            logger.error("Error crítico procesando el resultado de verificación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al procesar el resultado de verificación. Se reintentará.", e);
            throw new RuntimeException("Error en procesamiento asíncrono de resultado", e);
        }
    }
}
