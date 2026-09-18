package ar.unla.rentar.controller;

import ar.unla.rentar.dto.HistorialReservaDTO;
import ar.unla.rentar.model.Reserva;
import ar.unla.rentar.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class ReservaGraphQLController {

    @Autowired
    private ReservaRepository reservaRepository;

    @QueryMapping
    public List<HistorialReservaDTO> historialAlquileres(@Argument Long clienteId) {
        List<Reserva> reservas = reservaRepository.findByClienteId(clienteId);

        return reservas.stream().map(reserva -> {
            long dias = ChronoUnit.DAYS.between(reserva.getFechaInicio(), reserva.getFechaFin());                 //calcula los días entre la fecha de inicio y fin

            String nombreVehiculo = reserva.getVehiculo().getMarca() + " " + reserva.getVehiculo().getModelo();   //concatena marca y modelo

            return new HistorialReservaDTO(
                    nombreVehiculo,
                    reserva.getVehiculo().getPatente(),
                    reserva.getFechaInicio().toString(),
                    reserva.getFechaFin().toString(),
                    (int) dias,
                    reserva.getImporteTotal(),
                    reserva.getEstado().toString()
            );
        }).toList();
    }
}