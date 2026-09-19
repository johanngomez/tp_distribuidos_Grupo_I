package ar.unla.rentar.controller;

import ar.unla.rentar.dto.ReservaCreateDTO;
import ar.unla.rentar.dto.ReservaResponseDTO;
import ar.unla.rentar.dto.ReservaFiltroDTO;
import ar.unla.rentar.dto.HistorialReservaDTO;
import ar.unla.rentar.model.Reserva;
import ar.unla.rentar.model.Cliente;
import ar.unla.rentar.repository.ReservaRepository;
import ar.unla.rentar.service.ReservaService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class ReservaGraphQLController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @QueryMapping
    public List<Reserva> reservas(@Argument ReservaFiltroDTO filtro) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = auth.getName();

        return reservaService.consultarReservas(filtro, emailUsuario);
    }

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