package ar.unla.rentar.controller;

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
    public ResponseEntity<List<Vehiculo>> listarTodos() {
        return ResponseEntity.ok(vehiculoService.listarTodos());
    }

    // Get para buscar un vehículo por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Vehiculo> buscarPorId(@PathVariable Long id) {
        return vehiculoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Post para crear un nuevo vehículo
    @PostMapping
    public ResponseEntity<Vehiculo> crear(@Valid @RequestBody Vehiculo vehiculo) {
        Vehiculo nuevoVehiculo = vehiculoService.crear(vehiculo);

        return ResponseEntity
                .created(URI.create("/vehiculos/" + nuevoVehiculo.getId()))
                .body(nuevoVehiculo);
    }

    // Put para modificar un vehículo existente
    @PutMapping("/{id}")
    public ResponseEntity<Vehiculo> modificar(
            @PathVariable Long id,
            @Valid @RequestBody Vehiculo vehiculo) {

        Vehiculo vehiculoModificado = vehiculoService.modificar(id, vehiculo);

        return ResponseEntity.ok(vehiculoModificado);
    }

    // Delete para eliminar un vehículo (baja lógica)
    @DeleteMapping("/{id}")
    public ResponseEntity<Vehiculo> eliminar(@PathVariable Long id) {
        Vehiculo vehiculoEliminado = vehiculoService.eliminar(id);

        return ResponseEntity.ok(vehiculoEliminado);
    }
}