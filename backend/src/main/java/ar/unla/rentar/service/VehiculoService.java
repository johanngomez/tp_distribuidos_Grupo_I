package ar.unla.rentar.service;

import ar.unla.rentar.dto.VehiculoCreateDTO;
import ar.unla.rentar.dto.VehiculoResponseDTO;
import ar.unla.rentar.dto.VehiculoUpdateDTO;
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

    public List<VehiculoResponseDTO> listarTodos() {
        return vehiculoRepository.findAll() // busca todos los vehículos en la base de datos y los convierte a DTOs de respuesta
                .stream() // convierte la lista de vehículos a un stream para poder aplicar operaciones sobre ella
                .map(this::convertirAResponseDTO) // convierte cada vehículo a un DTO de respuesta
                .toList(); // convierte el stream de DTOs de respuesta a una lista y la devuelve
    }

    public Optional<VehiculoResponseDTO> buscarPorId(Long id) {
        return vehiculoRepository.findById(id)
                .map(this::convertirAResponseDTO);
    }

    public VehiculoResponseDTO crear(VehiculoCreateDTO dto) {

        if (vehiculoRepository.existsByPatente(dto.getPatente())) {
            throw new IllegalArgumentException("Ya existe un vehículo con esa patente");
        }

        Vehiculo vehiculo = new Vehiculo();

        vehiculo.setPatente(dto.getPatente());
        vehiculo.setMarca(dto.getMarca());
        vehiculo.setModelo(dto.getModelo());
        vehiculo.setAnio(dto.getAnio());
        vehiculo.setColor(dto.getColor());
        vehiculo.setTipo(dto.getTipo());
        vehiculo.setPrecioDiario(dto.getPrecioDiario());

        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setActivo(true);

        Vehiculo vehiculoGuardado = vehiculoRepository.save(vehiculo);

        return convertirAResponseDTO(vehiculoGuardado);
    }

    public VehiculoResponseDTO modificar(Long id, VehiculoUpdateDTO dto) {

        Vehiculo vehiculoExistente = vehiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un vehículo con ese ID"));

        vehiculoExistente.setMarca(dto.getMarca());
        vehiculoExistente.setModelo(dto.getModelo());
        vehiculoExistente.setAnio(dto.getAnio());
        vehiculoExistente.setColor(dto.getColor());
        vehiculoExistente.setTipo(dto.getTipo());
        vehiculoExistente.setPrecioDiario(dto.getPrecioDiario());
        vehiculoExistente.setEstado(dto.getEstado());

        Vehiculo vehiculoModificado = vehiculoRepository.save(vehiculoExistente);

        return convertirAResponseDTO(vehiculoModificado);
    }

    public VehiculoResponseDTO eliminar(Long id) {
        // Verificar si el vehículo con el ID proporcionado existe
        Vehiculo vehiculo = vehiculoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No existe un vehículo con ese ID"));

        // En lugar de eliminar físicamente el vehículo, se marca como inactivo baja lógica
        vehiculo.setActivo(false);
        Vehiculo vehiculoEliminado = vehiculoRepository.save(vehiculo); // Guardar los cambios en la base de datos

        return convertirAResponseDTO(vehiculoEliminado);  // Devolver el DTO del vehículo eliminado
    }

    private VehiculoResponseDTO convertirAResponseDTO(Vehiculo vehiculo) {

    VehiculoResponseDTO dto = new VehiculoResponseDTO();

    dto.setId(vehiculo.getId());
    dto.setPatente(vehiculo.getPatente());
    dto.setMarca(vehiculo.getMarca());
    dto.setModelo(vehiculo.getModelo());
    dto.setAnio(vehiculo.getAnio());
    dto.setColor(vehiculo.getColor());
    dto.setTipo(vehiculo.getTipo());
    dto.setPrecioDiario(vehiculo.getPrecioDiario());
    dto.setEstado(vehiculo.getEstado());
    dto.setActivo(vehiculo.getActivo());

    return dto;
    }
}