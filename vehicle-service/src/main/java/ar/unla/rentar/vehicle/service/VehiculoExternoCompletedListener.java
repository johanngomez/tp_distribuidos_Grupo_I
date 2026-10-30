package ar.unla.rentar.vehicle.service;

import ar.unla.rentar.vehicle.events.EventEnvelope;
import ar.unla.rentar.vehicle.model.Vehiculo;
import ar.unla.rentar.vehicle.repository.VehiculoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ar.unla.rentar.vehicle.model.EstadoVehiculo;

import java.util.Map;
import java.util.Optional;

@Component
public class VehiculoExternoCompletedListener {

    private final VehiculoRepository vehiculoRepository;
    private final ObjectMapper objectMapper;

    public VehiculoExternoCompletedListener(VehiculoRepository vehiculoRepository, ObjectMapper objectMapper) {
        this.vehiculoRepository = vehiculoRepository;
        this.objectMapper = objectMapper;
    }

    //escucha la cola exacta que definiste en tu RabbitMqConfig
    @RabbitListener(queues = "rentar.external.vehicle.completed")
    public void procesarValidacionCompletada(EventEnvelope eventoRecibido) {
        try {
            System.out.println("-> [CompletedListener] Recibido evento de validación final. CorrelationId: " + eventoRecibido.getCorrelationId());

            //extraer los datos del payload
            Map<String, Object> payloadMap = (Map<String, Object>) eventoRecibido.getPayload();
            Long vehiculoId = ((Number) payloadMap.get("vehiculoExternoId")).longValue();
            String resultadoFinal = (String) payloadMap.get("resultadoFinal");

            System.out.println("-> [CompletedListener] Vehículo ID: " + vehiculoId + " | Resultado final: " + resultadoFinal);

            //si ambas validaciones pasaron, pasamos el vehículo a DISPONIBLE
            if ("VALIDADO".equals(resultadoFinal)) {
                //buscamos el vehículo en la base de datos
                Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(vehiculoId);
                
                if (vehiculoOpt.isPresent()) {
                    Vehiculo vehiculo = vehiculoOpt.get();
                    
                    vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);                    
                    
                    vehiculoRepository.save(vehiculo);
                    System.out.println("-> [CompletedListener] ¡ÉXITO! Vehículo " + vehiculo.getPatente() + " actualizado a DISPONIBLE.");
                } else {
                    System.err.println("-> [CompletedListener] Error: No se encontró el vehículo con ID " + vehiculoId + " en la base de datos.");
                }
            } else {
                System.out.println("-> [CompletedListener] El vehículo fue RECHAZADO. No se incorpora a la flota.");
                //"Si una falla, no se incorpora", lo dejamos en PENDIENTE o se puede eliminar.
            }

        } catch (Exception e) {
            System.err.println("-> [CompletedListener] Error procesando evento final: " + e.getMessage());
        }
    }
}