package ar.unla.rentar.service;

import ar.unla.rentar.model.EstadoVehiculo;
import ar.unla.rentar.model.Vehiculo;
import ar.unla.rentar.repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service //el service es el que se encarga de la logica de negocio, es el que se encarga de hacer las operaciones con la base de datos
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    public List<Vehiculo> listarTodos() {
        return vehiculoRepository.findAll();
    }

    public Optional<Vehiculo> buscarPorId(Long id) {
        return vehiculoRepository.findById(id);
    }

    public Vehiculo crear(Vehiculo vehiculo) {

        // Verificar si ya existe un vehículo con la misma patente
        if (vehiculoRepository.existsByPatente(vehiculo.getPatente())) {
            throw new IllegalArgumentException("Ya existe un vehículo con esa patente");
        }
         // Establecer el estado y la disponibilidad del vehículo antes de guardarlo
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setActivo(true);

        return vehiculoRepository.save(vehiculo);
    }

    public Vehiculo modificar(Long id, Vehiculo vehiculo) {

        // Verificar si el vehículo con el ID proporcionado existe
        Vehiculo vehiculoExistente = vehiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un vehículo con ese ID"));

        vehiculoExistente.setMarca(vehiculo.getMarca());
        vehiculoExistente.setModelo(vehiculo.getModelo());
        vehiculoExistente.setAnio(vehiculo.getAnio());
        vehiculoExistente.setColor(vehiculo.getColor());
        vehiculoExistente.setTipo(vehiculo.getTipo());
        vehiculoExistente.setPrecioDiario(vehiculo.getPrecioDiario());
        vehiculoExistente.setEstado(vehiculo.getEstado());


        return vehiculoRepository.save(vehiculoExistente); // Guardar los cambios en la base de datos
    }

    public Vehiculo eliminar(Long id) {
        // Verificar si el vehículo con el ID proporcionado existe
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un vehículo con ese ID"));

        // En lugar de eliminar físicamente el vehículo, se marca como inactivo baja lógica
        vehiculo.setActivo(false);

        return vehiculoRepository.save(vehiculo);
    }
}