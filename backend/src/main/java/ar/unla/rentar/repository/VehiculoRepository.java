package ar.unla.rentar.repository;

import ar.unla.rentar.model.TipoVehiculo;
import ar.unla.rentar.model.Vehiculo; //importamos la clase Vehiculo para poder usarla en el repositorio
import org.springframework.data.jpa.repository.JpaRepository; //importamos JpaRepository para poder usar sus métodos y funcionalidades
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByPatente(String patente); //método para buscar un vehículo por su patente, devuelve un Optional que puede contener un Vehiculo o estar vacío si no se encuentra

    boolean existsByPatente(String patente);

    @Query("SELECT v FROM Vehiculo v WHERE v.activo = true " +
           "AND (:marca IS NULL OR v.marca = :marca) " +
           "AND (:modelo IS NULL OR v.modelo = :modelo) " +
           "AND (:tipo IS NULL OR v.tipo = :tipo) " +
           "AND (:precioMin IS NULL OR v.precioDiario >= :precioMin) " +
           "AND (:precioMax IS NULL OR v.precioDiario <= :precioMax) " +
           "AND NOT EXISTS (" +
           "    SELECT r FROM Reserva r WHERE r.vehiculo = v " +
           "    AND r.fechaInicio < :fechaFin " +
           "    AND r.fechaFin > :fechaInicio " +
           ")")
    List<Vehiculo> findDisponibles(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("tipo") TipoVehiculo tipo,
            @Param("marca") String marca,
            @Param("modelo") String modelo,
            @Param("precioMin") Double precioMin,
            @Param("precioMax") Double precioMax
    );

}