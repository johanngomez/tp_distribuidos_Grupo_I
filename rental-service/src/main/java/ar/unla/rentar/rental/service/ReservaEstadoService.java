package ar.unla.rentar.rental.service;

import ar.unla.rentar.rental.model.EstadoReserva;
import ar.unla.rentar.rental.model.Reserva;
import ar.unla.rentar.rental.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Mantiene los estados temporales de las reservas sin depender de otros servicios. */
@Service
public class ReservaEstadoService {
    private final ReservaRepository repository;

    public ReservaEstadoService(ReservaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void actualizarFinalizadas() {
        LocalDateTime ahora = LocalDateTime.now();
        var vencidas = repository.findAll().stream()
                .filter(reserva -> reserva.getFechaFin() != null)
                .filter(reserva -> reserva.getFechaFin().isBefore(ahora))
                .filter(reserva -> reserva.getEstado() == EstadoReserva.CONFIRMADA
                        || reserva.getEstado() == EstadoReserva.EN_CURSO)
                .peek(reserva -> reserva.setEstado(EstadoReserva.FINALIZADA))
                .toList();
        if (!vencidas.isEmpty()) {
            repository.saveAll(vencidas);
        }
    }
}
