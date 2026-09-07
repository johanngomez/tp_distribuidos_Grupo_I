package ar.unla.rentar.repository;

import ar.unla.rentar.model.Vehiculo; //importamos la clase Vehiculo para poder usarla en el repositorio
import org.springframework.data.jpa.repository.JpaRepository; //importamos JpaRepository para poder usar sus métodos y funcionalidades

import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByPatente(String patente); //método para buscar un vehículo por su patente, devuelve un Optional que puede contener un Vehiculo o estar vacío si no se encuentra

    boolean existsByPatente(String patente);
}