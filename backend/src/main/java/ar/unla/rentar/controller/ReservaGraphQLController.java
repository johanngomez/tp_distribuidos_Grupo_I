package ar.unla.rentar.controller;

import ar.unla.rentar.dto.ReservaCreateDTO;
import ar.unla.rentar.dto.ReservaResponseDTO;
import ar.unla.rentar.model.Reserva;
import ar.unla.rentar.service.ReservaService;
import ar.unla.rentar.dto.ReservaFiltroDTO;
import ar.unla.rentar.model.Cliente;
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

import java.util.List;

@Controller
public class ReservaGraphQLController {

    @Autowired
    private ReservaService reservaService;

    @QueryMapping
    public List<Reserva> reservas(@Argument ReservaFiltroDTO filtro) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = auth.getName();

        return reservaService.consultarReservas(filtro, emailUsuario);
    }
    
}
