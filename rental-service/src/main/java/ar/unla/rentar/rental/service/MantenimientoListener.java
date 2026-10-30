package ar.unla.rentar.rental.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import ar.unla.rentar.rental.dto.MantenimientoResultadoEvent;
import ar.unla.rentar.rental.dto.ReservaConfirmadaEvent;
import ar.unla.rentar.rental.model.ResultadoMantenimiento;

@Component
public class MantenimientoListener {
    private final MantenimientoService mantenimientoService;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE_NAME = "rentar.events";

    public MantenimientoListener(MantenimientoService mantenimientoService, RabbitTemplate rabbitTemplate) {
        this.mantenimientoService = mantenimientoService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "rentar.maintenance.request")
    public void consumirSolicitudMantenimiento(ReservaConfirmadaEvent evento) {
        // Simular el mantenimiento
        ResultadoMantenimiento resultado = mantenimientoService.simularMantenimiento(evento.vehiculoId());


        //crear el evento del resultado
        MantenimientoResultadoEvent resultadoEvent = new MantenimientoResultadoEvent(
            evento.reservaId(),
            evento.vehiculoId(),
            resultado
        );

        rabbitTemplate.convertAndSend("rentar.maintenance.result", resultadoEvent);

        System.out.println("Mantenimiento procesado para la reserva: " + evento.reservaId() + " -> " + resultado);



    }
}
