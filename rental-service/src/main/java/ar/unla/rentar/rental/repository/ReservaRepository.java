package ar.unla.rentar.rental.repository;
import ar.unla.rentar.rental.model.Reserva;
import ar.unla.rentar.rental.model.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface ReservaRepository extends JpaRepository<Reserva,Long> {
 List<Reserva> findByClienteId(Long id);
 boolean existsByVehiculoIdAndEstadoInAndFechaInicioLessThanAndFechaFinGreaterThan(Long id,List<EstadoReserva> estados,LocalDateTime fin,LocalDateTime inicio);
}
