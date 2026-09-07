package ar.unla.rentar.controller;

import ar.unla.rentar.dto.VehiculoCreateDTO;
import ar.unla.rentar.dto.VehiculoResponseDTO;
import ar.unla.rentar.dto.VehiculoUpdateDTO;
import ar.unla.rentar.model.Vehiculo;
import ar.unla.rentar.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController // anotación que indica que esta clase es un controlador de Spring MVC y que sus métodos manejarán solicitudes HTTP y devolverán respuestas HTTP.
@RequestMapping("/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    // Get para listar todos los vehículos
    @GetMapping
    public ResponseEntity<List<VehiculoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(vehiculoService.listarTodos());
    }

    // Get para buscar un vehículo por su ID
    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> buscarPorId(@PathVariable Long id) {
        return vehiculoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Post para crear un nuevo vehículo
    @PostMapping
    public ResponseEntity<VehiculoResponseDTO> crear(
            @Valid @RequestBody VehiculoCreateDTO dto) {

        VehiculoResponseDTO nuevoVehiculo = vehiculoService.crear(dto);

        return ResponseEntity
                .created(URI.create("/vehiculos/" + nuevoVehiculo.getId()))
                .body(nuevoVehiculo);
    }

    // Put para modificar un vehículo existente
    @PutMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> modificar(
            @PathVariable Long id,
            @Valid @RequestBody VehiculoUpdateDTO dto) {

        VehiculoResponseDTO vehiculoModificado = vehiculoService.modificar(id, dto);

        return ResponseEntity.ok(vehiculoModificado);
    }

    // Delete para eliminar un vehículo (baja lógica)
    @DeleteMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> eliminar(@PathVariable Long id) {
        VehiculoResponseDTO vehiculoEliminado = vehiculoService.eliminar(id);

        return ResponseEntity.ok(vehiculoEliminado);
    }
}