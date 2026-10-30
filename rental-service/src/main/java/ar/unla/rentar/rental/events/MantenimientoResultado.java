package ar.unla.rentar.rental.events;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ar.unla.rentar.rental.dto.MantenimientoResultadoEvent;
import ar.unla.rentar.rental.model.Reserva;
import ar.unla.rentar.rental.repository.ReservaRepository;

@Component
public class MantenimientoResultado {

    @Autowired
    private ReservaRepository reservaRepository
    ;
    // Escucha la cola declarada para el resultado del mantenimiento
    @RabbitListener(queues = "rentar.maintenance.result")
    public void consumirResultadoMantenimiento(MantenimientoResultadoEvent evento) {
        //Buscamos la reserva en la base de datos usando el ID del evento
        Reserva reserva = reservaRepository.findById(evento.reservaId()).orElseThrow(
        () -> new RuntimeException("Reserva no encontrada con ID: " + evento.reservaId())
    );

        //si la reserva si existe
        if (reserva != null) {
            //Actualizamos el campo correspondiente con el resultado ("REALIZADO" o "NO_REALIZADO")
            reserva.setResultadoMantenimiento(evento.resultado());
            //Guardamos los cambios en la base de datos
            reservaRepository.save(reserva);
            System.out.println("Reserva " + evento.reservaId() + " actualizada con éxito.");
        }


    }
}
