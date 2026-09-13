package ar.unla.rentar.controller;

import ar.unla.rentar.dto.ReservaCreateDTO;
import ar.unla.rentar.dto.ReservaResponseDTO;
import ar.unla.rentar.model.Reserva;
import ar.unla.rentar.service.ReservaService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @GetMapping
    public ResponseEntity<List<Reserva>> listarTodas() {
        return ResponseEntity.ok(reservaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crearReserva(@Valid @RequestBody ReservaCreateDTO dto) {
    ReservaResponseDTO nuevaReserva = reservaService.crearReserva(dto);
        return new ResponseEntity<>(nuevaReserva, HttpStatus.CREATED);
    }
}