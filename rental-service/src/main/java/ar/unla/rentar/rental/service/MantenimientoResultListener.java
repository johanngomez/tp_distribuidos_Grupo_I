package ar.unla.rentar.rental.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.unla.rentar.rental.dto.MantenimientoResultadoEvent;
import ar.unla.rentar.rental.model.Reserva;
import ar.unla.rentar.rental.repository.ReservaRepository;

@Component
public class MantenimientoResultListener {

    @Autowired 
    private ReservaRepository reservaRepository;

    @RabbitListener(queues = "rentar.maintenance.result")
    public void recibirResultadoMantenimiento(MantenimientoResultadoEvent evento) {

         Reserva reserva = reservaRepository.findById(evento.reservaId()).orElseThrow(
        () -> new RuntimeException("Reserva no encontrada con ID: " + evento.reservaId())
    );

        if (reserva != null) {
            reserva.setResultadoMantenimiento(evento.resultado());
            reservaRepository.save(reserva);
        }

        System.out.println("Resultado de mantenimiento recibido para la reserva: " + evento.reservaId());
    }

}
