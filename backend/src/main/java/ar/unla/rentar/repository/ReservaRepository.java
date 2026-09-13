package ar.unla.rentar.repository;

import ar.unla.rentar.model.Reserva;
import ar.unla.rentar.model.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByEstado(EstadoReserva estado);
    
    List<Reserva> findByClienteId(Long clienteId);

    boolean existsByVehiculoIdAndEstadoInAndFechaInicioLessThanAndFechaFinGreaterThan(
        Long vehiculoId,
        List<EstadoReserva> estados,
        java.time.LocalDateTime fechaFin,
        java.time.LocalDateTime fechaInicio);
}
